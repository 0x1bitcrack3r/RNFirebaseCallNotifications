import React, { Component } from 'react';
import { StyleSheet, Text, View } from 'react-native';
import firebase from 'react-native-firebase';

class App extends Component {
  constructor(props) {
    super(props);

    this.state = {
      token: '',
    };
  }

  componentDidMount() {
    firebase
      .messaging()
      .getToken()
      .then((token) => {
        console.log('Device FCM Token: ', token);
        this.setState({ token });
      });
  }

  render() {
    return (
      <View style={styles.container}>
        <Text style={styles.welcome}>Welcome to React Native!</Text>
        <Text selectable>{this.state.token}</Text>
      </View>
    );
  }
}

const styles = StyleSheet.create({
  container: {
    justifyContent: 'center',
    alignItems: 'center',
    flex: 1,
  },
  welcome: {
    fontSize: 22,
    color: 'red',
  },
});

export default App;
