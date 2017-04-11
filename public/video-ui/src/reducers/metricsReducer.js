export default function metrics(state = null, action) {
  switch (action.type) {
    case 'METRICS_GET_RECEIVE':
      return action.metrics;

    default:
      return state;
  }
}
