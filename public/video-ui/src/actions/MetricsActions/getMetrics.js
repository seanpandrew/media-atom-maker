import MetricsApi from '../../services/MetricsApi';

function receiveMetrics(metrics) {
  return {
    type: 'METRICS_GET_RECEIVE',
    metrics: metrics,
    receivedAt: Date.now()
  };
}

function errorReceivingMetrics(error) {
  return {
    type:       'SHOW_ERROR',
    message:    `Could not get metrics ${error}`,
    error:      error,
    receivedAt: Date.now()
  };
}

export function getMetrics() {
  return dispatch => {
    return MetricsApi.getMetrics()
        .then(res => {
            dispatch(receiveMetrics(res));
        })
        .catch(error => dispatch(errorReceivingMetrics(error)));
  };
}
