import UploadsApi from '../../services/UploadsApi';

function startUploadAction() {
  return {
    type: 'START_UPLOAD',
    receivedAt: Date.now()
  };
}

function uploadProgressAction(completed, total) {
  return {
    type: 'UPLOAD_PROGRESS',
    receivedAt: Date.now(),
    completed: completed,
    total: total
  };
}

function uploadComplete() {
  return {
    type: 'UPLOAD_COMPLETE',
    receivedAt: Date.now()
  };
}

export function startUpload(policy, file) {
  return dispatch => {
    dispatch(startUploadAction());

    function progress(completed, total) {
      dispatch(uploadProgressAction(completed, total));
    }

    UploadsApi.startUpload(policy, file, progress).then(() => {
      dispatch(uploadComplete());
    });
  };
}