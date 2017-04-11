import React from 'react';
import Measure from 'react-measure';

class Graph extends React.Component {
    renderGraph = ({width, height}) => {
        if(width === 0 || height === 0) {
            return <div className="dashboard__placeholder" />;
        }

        const series = this.props.data.map((s) => s.join(","));
        const data = `chd=t:${series.join("|")}`;

        const background = `chf=bg,s,333333`;
        const colours = `chco=${this.props.colours.join(",")}`;

        const dimensions = `chs=${Math.floor(width)}x${Math.floor(height)}`;
        const margins = "chma=5,5,5,5";

        const params = ["cht=lc:nda", "chds=a", dimensions, background, colours, margins, data];
        const url = `https://chart.googleapis.com/chart?${params.join("&")}`;

        return <img src={url} width={width} height={height} />;
    };

    render() {
        return <Measure>{this.renderGraph}</Measure>;
    }
}

function CostMetrics() {
    return <div className="dashboard__headline">
        <div className="dashboard__headline__left">

        </div>
        <div className="dashboard__headline__right">
            <strong>$</strong>
        </div>
    </div>;
}

function PublishTimeMetrics({ today, metrics }) {
    const avgTimeToday = Math.ceil(today.newRoute.avgPublishTime / 1000 / 60);
    const avgTimeMins = metrics.map((day) => Math.ceil(day.newRoute.avgPublishTime / 1000 / 60));

    return <div className="dashboard__headline">
        <div className="dashboard__headline__left">
            <Graph data={[avgTimeMins]} colours={["ffbc01"]} />
        </div>
        <div className="dashboard__headline__right">
            <strong>{avgTimeToday}</strong>
            <sub>mins</sub>
        </div>
    </div>;
}

function OldVsNewMetrics({ today, metrics }) {
    const oldVideos = metrics.map((metric) => metric.oldRoute.videos);
    const newVideos = metrics.map((metric) => metric.newRoute.videos);

    return <div className="dashboard__headline">
        <div className="dashboard__headline__left">
            <Graph data={[newVideos, oldVideos]} colours={["148ad2", "ff7272"]} />
        </div>
        <div className="dashboard__headline__right">
            <strong>{today.newRoute.videos}</strong>
            <sub>{today.oldRoute.videos}</sub>
        </div>
    </div>;
}

class Dashboard extends React.Component {
    componentWillMount() {
        this.props.metricsActions.getMetrics();
    }

    renderTop() {
        if(this.props.metrics === null) {
            return <div className="dashboard__top">Loading</div>;
        }

        const today = this.props.metrics.dayMetrics[0];
        const metrics = this.props.metrics.dayMetrics.slice().reverse();

        return <div className="dashboard__top">
            <OldVsNewMetrics today={today} metrics={metrics} />
            <PublishTimeMetrics today={today} metrics={metrics} />
            <CostMetrics />
        </div>;
    }

    render() {
        return <div className="dashboard">
            {this.renderTop()}
            <div className="dashboard__bottom">
                {/* TODO */}
            </div>
        </div>;
    }
}

//REDUX CONNECTIONS
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';
import * as getMetrics from '../../actions/MetricsActions/getMetrics';

function mapStateToProps(state) {
  return {
    metrics: state.metrics
  };
}

function mapDispatchToProps(dispatch) {
  return {
    metricsActions: bindActionCreators(Object.assign({}, getMetrics), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(Dashboard);