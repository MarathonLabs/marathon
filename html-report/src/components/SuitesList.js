import React, { Component } from 'react';
import convertTime from './../utils/convertTime';
import paths from './../utils/paths';

export default class SuitesList extends Component {
  render() {
    return (
      <div className="content margin-top-20">
        <div className="title-common">Suites list</div>

        { window.mainData.suites.map((suite) => {
            return (
              <div key={ suite.id } className="suite-item card">
                <a href={ paths.fromIndexToSuite(suite.id) } className="title-common with-arrow">
                  Suite { suite.id  }
                </a>
                <div className="row full margin-bottom-20 bounded">
                  <div className="card-info">
                    <div className="text-sub-title-light">Passed</div>
                    <div className="card-info__content status-passed">{ suite.passed_count }</div>
                  </div>
                  <div className="card-info">
                    <div className="text-sub-title-light">Failed</div>
                    <div className="card-info__content status-failed">{ suite.failed_count }</div>
                  </div>
                  <div className="card-info">
                    <div className="text-sub-title-light">Ignored</div>
                    <div className="card-info__content status-ignored">{ suite.ignored_count }</div>
                  </div>
                  <div className="card-info">
                    <div className="text-sub-title-light">Duration</div>
                    <div className="card-info__content">{ convertTime(suite.duration_millis) }</div>
                  </div>
                  <div className="card-info">
                    <div className="text-sub-title-light">Devices</div>
                    <div className="card-info__content">{ suite.devices.length }</div>
                  </div>
                </div>
                <ul className="container-expanded list">
                  { suite.devices.map((device, i) => {
                    return (<li key={ i } className="list__item no-hover">
                        <div className="text-title margin-bottom-10 label">{ device.id }</div>
                        <div className="text-title margin-bottom-10 label">{ device.model }</div>
                        <div className="margin-bottom-10">
                          <a href={ device.logcat_path }>{ device.logcat_path }</a>
                        </div>
                        <div className="margin-bottom-10">
                          <a href={ device.instrumentation_output_path }>{ device.instrumentation_output_path }</a>
                        </div>
                      </li>
                    )
                  }
                  )}
                </ul>
              </div>
            )
          }
        )}
      </div>
    );
  }
}
