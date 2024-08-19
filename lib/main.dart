import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: BluetoothDeviceScreen(),
    );
  }
}

class BluetoothDeviceScreen extends StatefulWidget {
  @override
  _BluetoothDeviceScreenState createState() => _BluetoothDeviceScreenState();
}

class _BluetoothDeviceScreenState extends State<BluetoothDeviceScreen> {
  static const platform =
      MethodChannel('com.example.test_bluetooth_devices/bluetooth');

  List<String> _devices = [];

  @override
  void initState() {
    super.initState();
    _getBluetoothDevices();
  }

  Future<void> _getBluetoothDevices() async {
    try {
      final List<dynamic> devices =
          await platform.invokeMethod('getBluetoothDevices');
      setState(() {
        _devices = devices.cast<String>();
      });
    } on PlatformException catch (e) {
      print("Failed to get Bluetooth devices: '${e.message}'.");
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('Bluetooth Devices'),
      ),
      body: ListView.builder(
        itemCount: _devices.length,
        itemBuilder: (context, index) {
          return ListTile(
            title: Text(_devices[index]),
          );
        },
      ),
    );
  }
}
