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
            this.onDataReceived(text);
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
                        {!!this.state.logs && this.state.logs.split("\n")
                            .filter(v=>v!='')
                            .map((line) => {
                            const arr = line.split(" ");
                            const time = arr[0] + " " + arr[1];
                            const process = arr[2].split("-")[0];
                            const logArr = arr[3].split("/");
                            const level = logArr[0];
                            const tag = logArr[1].substring(0, logArr[1].length-1);
                            const message = line.split(tag + ": ");

                                function selectStyle(logLevel) {
                                    switch (logLevel) {
                                        case "W":
                                            return "line warn";
                                        case "D": {
                                            return "line debug";
                                        }
                                        case "E": {
                                            return "line error";
                                        }
                                        case "I": {
                                            return "line info";
                                        }
                                        case "A": {
                                            return "line assert";
                                        }
                                        case "V": {
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
