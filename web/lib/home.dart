import 'package:flutter/material.dart';

import 'chat.dart';
import 'status.dart';

class HomePage extends StatelessWidget {
  HomePage({Key key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Column(
        mainAxisSize: MainAxisSize.max,
        children: <Widget>[
          StatusBar(),
          ChatPanel(),
        ],
      ),
    );
  }
}
