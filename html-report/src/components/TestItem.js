import React, {Component} from 'react';
import cx from 'classnames';
import convertTime from './../utils/convertTime'
import paths from './../utils/paths'
import ReactPlayer from 'react-player'

export default class TestItem extends Component {
    componentWillMount() {
        document.title = `Test ${window.test.name}`;
    }

    render() {
        const data = window.test;
        let statusLabelClass = cx('label', 'margin-right-10', {
            alert: data.status === 'failed',
            success: data.status === 'passed'
        });

        return (
            <div className="content margin-top-20">
                <div className="title-common vertical-aligned-content">
                    <a href={paths.fromTestToIndex}>Pools list</a> /
                    <a href={paths.fromTestToPool(data.pool_id)}>Pool {data.pool_id}</a> /
                    {data.deviceId}
                </div>
                <div className='margin-top-20'>
                    <div className={`card row full justify-between test-page ${data.status}`}>
                        <div className="margin-right-20">
                            <div className="margin-bottom-10 vertical-aligned-content">
                                <div className={statusLabelClass}>{data.status}</div>
                                <span className={`test-page__title status-${data.status}`}>{data.name}</span></div>
                            <div className="title-l text-sub-title margin-bottom-5">{data.class_name}</div>
                            <div className="margin-bottom-5">{data.package_name}</div>
                        </div>
                        <div className="card-info__content">{convertTime(data.duration_millis)}</div>
                        <div className="card-info__content">
                            <a href={paths.fromTestToLogs(data.filename)}>Log file</a>
                        </div>
                    </div>

                    {!!data.video && <div className='card row full'>
                        <ReactPlayer url={data.video} controls={true} playsinline={true}/>
                    </div>}

                    {!!data.screenshot && <div className='card row full'>
                        <img src={data.screenshot}/>
                    </div>}
                </div>
            </div>
        );
    }
}
