#!/bin/sh
docker build . -t terraria-control-panel
docker run --rm -v "`PWD`":/out_dir --entrypoint /bin/sh terraria-control-panel -c "cp /app/TerrariaControlPanel /out_dir"
echo
echo
echo "To run the docker container execute:"
echo "    $ docker run -p 8080:8080 terraria-control-panel"
