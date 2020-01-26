import React, {Component} from 'react';
import convertTime from './../utils/convertTime';
import paths from './../utils/paths';

export default class HomePage extends Component {

    componentDidMount() {
        document.title = window.mainData.title;
    }

    render() {
        return (
            <div className="content margin-top-20">
                <div className="title-common">Pools</div>

                {window.mainData.pools.map((pool) => {
                        return (
                            <div key={pool.id} className="suite-item card">
                                <a href={paths.fromIndexToPool(pool.id)} className="title-common with-arrow">
                                    Pool {pool.id}
                                </a>
                                <div className="row full margin-bottom-20 bounded">
                                    <div className="card-info">
                                        <div className="text-sub-title-light">Passed</div>
                                        <div className="card-info__content status-passed">{pool.passed_count}</div>
                                    </div>
                                    <div className="card-info">
                                        <div className="text-sub-title-light">Failed</div>
                                        <div className="card-info__content status-failed">{pool.failed_count}</div>
                                    </div>
                                    <div className="card-info">
                                        <div className="text-sub-title-light">Ignored</div>
                                        <div className="card-info__content status-ignored">{pool.ignored_count}</div>
                                    </div>
                                    <div className="card-info">
                                        <div className="text-sub-title-light">Duration</div>
                                        <div className="card-info__content">{convertTime(pool.duration_millis)}</div>
                                    </div>
                                    <div className="card-info">
                                        <div className="text-sub-title-light">Devices</div>
                                        <div className="card-info__content">{pool.devices.length}</div>
                                    </div>
                                </div>
                            </div>
                        )
                    }
                )}

                <div className="title-common">Timeline</div>

                <div className="suite-item card">
                    <iframe src="timeline/index.html" style={{ width: "100%", height: "137" }}></iframe>
                </div>
            </div>
        );
    }
}
