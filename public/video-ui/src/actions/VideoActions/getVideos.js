import VideosApi from '../../services/VideosApi';
import capi from '../../services/capi';

function requestVideos() {
  return {
    type:       'VIDEOS_GET_REQUEST',
    receivedAt: Date.now()
  };
}

function receiveVideos(videos) {
  return {
    type:       'VIDEOS_GET_RECEIVE',
    videos:     videos,
    receivedAt: Date.now()
  };
}

function errorReceivingVideos(error) {
  return {
    type:       'SHOW_ERROR',
    message:    'Could not get videos',
    error:      error,
    receivedAt: Date.now()
  };
}

export function getVideos() {
  return dispatch => {
    dispatch(requestVideos());
    return VideosApi.fetchVideos()
        .catch(error => dispatch(errorReceivingVideos(error)))
        .then(res => {
          dispatch(receiveVideos(res));
        });
  };
}

export function searchVideosWithQuery(query) {
  return dispatch => {
    dispatch(requestVideos());
    return capi.searchTerm(query)
      .catch(error => dispatch(errorReceivingVideos(error)))
      .then(res => {
        dispatch(receiveVideos(res));
      });
  };
}
