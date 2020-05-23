FROM oracle/graalvm-ce:20.1.0-java11 as graalvm
# For JDK 11
#FROM oracle/graalvm-ce:20.0.0-java11 as graalvm
RUN gu install native-image

COPY . /home/app/TerrariaControlPanel
WORKDIR /home/app/TerrariaControlPanel

RUN native-image --no-server -cp build/libs/TerrariaControlPanel-*-all.jar

FROM frolvlad/alpine-glibc
RUN apk update && apk add libstdc++
EXPOSE 8080
COPY --from=graalvm /home/app/TerrariaControlPanel/TerrariaControlPanel /app/TerrariaControlPanel
ENTRYPOINT ["/app/TerrariaControlPanel"]
