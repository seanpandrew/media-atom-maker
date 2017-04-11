import React from 'react';

function Headline({right}) {
    return <div className="dashboard__headline">
        <div className="dashboard__headline__left">

        </div>
        <div className="dashboard__headline__right">
            {right.map((line) => <div key={line}>{line}</div>)}
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
        const avgTime = Math.ceil(today.newRoute.avgPublishTime / 1000 / 60);

        return <div className="dashboard__top">
            <Headline right={[today.newRoute.videos, today.oldRoute.videos]} />
            <Headline right={[avgTime, "mins"]} />
            <Headline right={["€"]} />
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