import 'dart:async';
import 'dart:convert';
import 'dart:html';

import 'package:flutter/material.dart';

import 'config.dart';

class StatusBar extends StatefulWidget {
  StatusBar({Key key}) : super(key: key);

  @override
  _StatusBarState createState() => _StatusBarState();
}

enum ServerState { ON, OFF, SHUTTING_DOWN }

final stateNames = {
  ServerState.ON: 'On',
  ServerState.OFF: 'Off',
  ServerState.SHUTTING_DOWN: 'Shutting',
};

final stateBtnNames = {
  ServerState.ON: 'Turn off',
  ServerState.OFF: 'Turn on',
  ServerState.SHUTTING_DOWN: 'Force shutdown',
};

class _StatusBarState extends State<StatusBar> {
  ServerState state;

  Timer timer;

  void updateStats() async {
    final res = await HttpRequest.request(
      Config.SERVER_STATUS,
      withCredentials: true,
    );
    Map<String, dynamic> json = jsonDecode(res.responseText);
    final isOn = json['open'] as bool;
    if (state != ServerState.SHUTTING_DOWN || !isOn) {
      setState(() {
        state = isOn ? ServerState.ON : ServerState.OFF;
      });
    }
  }

  void onPressed(BuildContext context) async {
    var force = false;
    if (state == ServerState.SHUTTING_DOWN) {
      final res = await confirmShutdown(context);
      if (res != true) {
        return;
      }
      force = true;
    }
    if (state == ServerState.ON || state == ServerState.SHUTTING_DOWN) {
      await HttpRequest.request(
        Config.SERVER_STOP + "?force=$force",
        withCredentials: true,
      );
      setState(() {
        state = ServerState.SHUTTING_DOWN;
      });
    } else if (state == ServerState.OFF) {
      await HttpRequest.request(
        Config.SERVER_START,
        withCredentials: true,
      );
    }
    updateStats();
  }

  @override
  void initState() {
    super.initState();
    timer = Timer.periodic(const Duration(seconds: 10), (timer) {
      updateStats();
    });
    updateStats();
  }

  @override
  void dispose() {
    timer.cancel();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    if (state == null) {
      return Expanded(
        child: Center(
          child: CircularProgressIndicator(),
        ),
      );
    }
    return DecoratedBox(
      decoration: BoxDecoration(
        color: Colors.grey,
      ),
      child: Padding(
        padding: const EdgeInsets.all(12.0),
        child: Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            buildStatusBadge(context),
            buildTurnButton(context),
          ],
        ),
      ),
    );
  }

  Widget buildStatusBadge(BuildContext context) {
    return DecoratedBox(
      decoration: BoxDecoration(
        color: state == ServerState.ON ? Colors.green : Colors.red,
        borderRadius: BorderRadius.all(Radius.circular(24.0)),
      ),
      child: SizedBox(
        height: 32.0,
        width: 128.0,
        child: Center(
          child: Text(
            stateNames[state],
            style: Theme.of(context).textTheme.headline6.copyWith(
                  color: Colors.white,
                ),
          ),
        ),
      ),
    );
  }

  Widget buildTurnButton(BuildContext context) {
    return RaisedButton(
      onPressed: () => onPressed(context),
      color: state == ServerState.OFF ? Colors.green : Colors.red,
      child: Text(stateBtnNames[state]),
    );
  }

  Future<bool> confirmShutdown(BuildContext context) {
    return showDialog<bool>(
        context: context,
        builder: (context) {
          return AlertDialog(
            title: Text('Server ir already shutting down'),
            content: Text('Do you want to force the shutdown right now?'),
            actions: [
              FlatButton(
                child: Text('Kill server'),
                onPressed: () {
                  Navigator.of(context).pop(true);
                },
              ),
              RaisedButton(
                child: Text('Wait'),
                onPressed: () {
                  Navigator.of(context).pop();
                },
              ),
            ],
          );
        });
  }
}
