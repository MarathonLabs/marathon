import React, {Component} from 'react';
import ReactLoading from 'react-loading';
import paths from "../utils/paths";

export default class ColorizedLogsComponent extends Component {
    state = {
        data: window.logs,
        logs: null
    };

    componentWillMount() {
        this.loadData(window.logs.log_path, function (text) {
            const data = JSON.parse(text);
            this.onDataReceived(data);
        }.bind(this));
    }

    onDataReceived(data) {
        this.setState({logs: data})
    }

    loadData(file, callback) {
        var rawFile = new XMLHttpRequest();
        rawFile.overrideMimeType("application/json");
        rawFile.open("GET", file, true);
        rawFile.onreadystatechange = function () {
            if (rawFile.readyState === 4) {
                if (rawFile.status === 200) {
                    callback(rawFile.responseText);
                } else {
                    callback("[]");
                }
            }
        };
        rawFile.send(null);
    }


    render() {
        return (
            <div className="content margin-top-20">
                <div className="title-common vertical-aligned-content">
                    <a href={paths.fromLogsToIndex}>Pools list</a> /
                    <a href={paths.fromLogsToPool(this.state.data.pool_id)}>Pool {this.state.data.pool_id}</a> /
                    <a href={paths.fromLogsToTest(this.state.data.test_id)}>{this.state.data.display_name}</a> /
                    Logs
                </div>


                <div className="card">
                    <table className="table logcat">
                        <tbody>
                        <tr>
                            <th>Process</th>
                            <th>Tag</th>
                            <th>Level</th>
                            <th>Time</th>
                            <th className="message">Message</th>
                        </tr>
                        {!!this.state.logs && this.state.logs.map((log) => {
                                const process = log.mHeader.mPid;
                                const tag = log.mHeader.mTag;
                                const level = log.mHeader.mLogLevel;
                                const timestamp = log.mHeader.mTimestamp;
                                const time = timestamp.mMonth + "-" + timestamp.mDay + " " + timestamp.mHour + ":" + timestamp.mMinute + ":" + timestamp.mSecond + "." + timestamp.mMilli;
                                const message = log.mMessage;

                                function selectStyle(logLevel) {
                                    switch (logLevel) {
                                        case "WARN":
                                            return "line warn";
                                        case "DEBUG": {
                                            return "line debug";
                                        }
                                        case "ERROR": {
                                            return "line error";
                                        }
                                        case "INFO": {
                                            return "line info";
                                        }
                                        case "ASSERT": {
                                            return "line assert";
                                        }
                                        case "VERBOSE": {
                                            return "line verbose";
                                        }
                                    }
                                }

                                return (<tr className={selectStyle(level)}>
                                    <td>
                                        {process}
                                    </td>
                                    <td>
                                        {tag}
                                    </td>
                                    <td>{level}</td>
                                    <td className="formatted-time">{time}</td>
                                    <td>{message}</td>
                                </tr>);
                            }
                        )}
                        </tbody>
                    </table>
                    {this.state.logs == null && <ReactLoading className="center"
                                                              type="bubbles"
                                                              color="#ff0000"
                                                              delay="1"/>}
                </div>
            </div>
        );
    }
}