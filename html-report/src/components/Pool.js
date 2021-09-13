import React, {Component} from 'react';
import cx from 'classnames';
import randomColor from 'randomcolor';
import convertTime from './../utils/convertTime';
import paths from './../utils/paths';
import SearchBar from './SearchBar';

export default class Pool extends Component {
  state = {
    colors: null,
    tests: window.pool.tests
  };

  componentWillMount() {
    this.setDevicesLabelColors();
    document.title = `Pool ${window.pool.id}`;
  }

  setDevicesLabelColors() {
    const generatedColors = randomColor({
      count: window.pool.devices.length,
      luminosity: 'bright'
    });
    let colors = {};
    window.pool.devices.map((item, i) => {
      colors[item.id] = generatedColors[i];
    });
    this.setState({ colors });
  }

  getSearchResults(results) {
    this.setState({ tests: results });
  }

  render() {
    const data = window.pool;
    return (
      <div className="content margin-top-20">
        <div className="title-common"><a href={ paths.fromPoolToIndex }>Pools list</a>/ Pool {data.id}</div>

        <SearchBar setSearchResults={ (results) => this.getSearchResults(results) } />

        <div className="card">
          <div className="vertical-aligned-content title-common">
            <div className="margin-right-10">Tests</div>
            <span className="label">{ this.state.tests.length }</span>
          </div>
          <div className="container-expanded list">
            { this.state.tests.map((test, i) => {
                return (<a key={i} href={`${data.id}/${test.deviceId}/${test.filename}`}
                           className={cx('list__item', 'row full justify-between', test.status)}>
                    <div>
                        <div className="margin-bottom-5 text-sub-title">{test.name}</div>
                        <div className="title-l text-sub-title margin-bottom-5 margin-right-10">{test.class_name}</div>
                        <div className="margin-bottom-5">{test.package_name}</div>
                    </div>
                    <div className="labels-list">
                        <div className="margin-bottom-5">
                    <span className="label info"
                          style={{background: this.state.colors[test.deviceId]}}>{test.deviceId}</span>
                        </div>
                        <div className="margin-bottom-5">
                            <span className="label big">{convertTime(test.duration_millis)}</span>
                        </div>
                    </div>
                </a>)
            }) }
          </div>
        </div>
      </div>
    );
  }
}
