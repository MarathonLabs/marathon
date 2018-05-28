import React, { Component } from 'react';
import Suite from './components/Suite'
import SuitesList from './components/SuitesList'
import TestItem from './components/TestItem'

class App extends Component {
  renderComponent() {
    if (window.mainData) {
      return <SuitesList />
    }
    if (window.test) {
      return <TestItem />
    }
    if (window.suite) {
      return <Suite />
    }
    return null;
  }

  render() {
    return (
      <div className="page">
        <div className="page-content">
          { this.renderComponent() }
          {/*"/suites/:suiteId/tests/:deviceId/:testId" />*/}
        </div>
      </div>
    );
  }
}

export default App;
