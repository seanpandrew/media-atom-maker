import { pandaReqwest } from './pandaReqwest';
import { getStore } from '../util/storeAccessor';

export default class ContentApi {
  static get published() {
    return 'published';
  }

  static get preview() {
    return 'preview';
  }

  static getUrl(stage) {
    return stage === ContentApi.published
      ? ContentApi.liveProxyUrl
      : ContentApi.proxyUrl;
  }

  static get proxyUrl() {
    return getStore().getState().config.capiProxyUrl;
  }

  static get liveProxyUrl() {
    return getStore().getState().config.liveCapiProxyUrl;
  }

  static search(query) {
    const encodedQuery = encodeURIComponent(query);

    return pandaReqwest({
      url: `${ContentApi.proxyUrl}/atoms?types=media&q=${encodedQuery}&searchFields=data.title`
    });
  }

  static getByPath(path, retry = false) {
    const retryTimeout = retry ? 10 * 1000 : 0; // retry up to 10 seconds

    return pandaReqwest(
      {
        url: `${ContentApi.proxyUrl}/${path}?show-fields=all`
      },
      retryTimeout
    );
  }

  static getLivePage(id) {
    return pandaReqwest({
      url: `${ContentApi.liveProxyUrl}/${id}`
    });
  }

  static getTagsByType(query, types) {
    return Promise.all(
      types.map(type => {
        if (query === '*') {
          return pandaReqwest({
            url: `${ContentApi.proxyUrl}/tags?page-size=100&type=${type}` //TODO this is likely to change based on CAPI work to search by prefix on webTitle
          });
        }
        const encodedQuery = encodeURIComponent(query);
        return pandaReqwest({
          url: `${ContentApi.proxyUrl}/tags?page-size=200&type=${type}&web-title=${encodedQuery}`
        });
      })
    );
  }
}
