import 'dart:async';
import 'dart:collection';
import 'dart:html';

import 'package:http/http.dart' as http;
import 'package:flutter/material.dart';
import 'package:web_socket_channel/html.dart';
import 'package:web_socket_channel/web_socket_channel.dart';

import 'config.dart';

class ChatPanel extends StatefulWidget {
  ChatPanel({Key key}) : super(key: key);

  @override
  _ChatPanelState createState() => _ChatPanelState();
}

class _ChatPanelState extends State<ChatPanel> {
  final lines = DoubleLinkedQueue<String>();

  WebSocketChannel socket;
  StreamSubscription sub;

  final _controller = TextEditingController();
  final _editFocus = FocusNode();

  final controller = ScrollController();

  @override
  void initState() {
    super.initState();
    (() async {
      final req = await HttpRequest.request(Config.QUERY_WS_TOKEN,
          withCredentials: true);
      final token = req.responseText;
      socket = HtmlWebSocketChannel.connect(Config.WEB_SOCKET + token);
      sub = socket.stream.listen((event) {
        setState(() {
          lines.addFirst(event);
        });
      });
      setState(() {});
    })();
  }

  @override
  void dispose() {
    if (socket != null) {
      socket.sink.close();
      sub.cancel();
    }
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    if (socket == null) {
      return Expanded(
        child: Center(
          child: CircularProgressIndicator(),
        ),
      );
    }
    final listStyle = Theme.of(context).textTheme.bodyText1;
    return Expanded(
      child: Padding(
        padding: const EdgeInsets.all(8.0),
        child: Column(
          mainAxisSize: MainAxisSize.max,
          mainAxisAlignment: MainAxisAlignment.end,
          children: [
            Expanded(
              child: ListView(
                controller: controller,
                shrinkWrap: true,
                reverse: true,
                children: [
                  for (final text in lines)
                    DecoratedBox(
                      decoration: BoxDecoration(
                        border: Border(bottom: BorderSide(color: Colors.grey)),
                      ),
                      child: Padding(
                        padding: const EdgeInsets.all(8.0),
                        child: Text(text, style: listStyle),
                      ),
                    ),
                ],
              ),
            ),
            TextField(
              controller: _controller,
              focusNode: _editFocus,
              onSubmitted: (str) {
                if (str.isNotEmpty) {
                  setState(() {
                    // lines.addLast(str);
                    socket.sink.add(str);
                    _controller.clear();
                  });
                  FocusScope.of(context).requestFocus(_editFocus);
                }
              },
              decoration: InputDecoration(
                border: UnderlineInputBorder(),
                hintText: 'Say something or /help',
              ),
            )
          ],
        ),
      ),
    );
  }
}
