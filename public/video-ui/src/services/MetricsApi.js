import {pandaReqwest} from './pandaReqwest';

export default {
    getMetrics: () => {
        const url = "/api2/metrics";

        return pandaReqwest({
            url: url,
            method: 'get'
        });
    }
};