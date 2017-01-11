import React from 'react';
import validate from '../../constants/videoEditValidation';
import videoEditFields from '../../constants/videoEditFields';
import VideoEditDisplay from './VideoEditDisplay';
import { Fields, reduxForm } from 'redux-form';

const VideoEdit = (props) => {
  return (
    <div>
      <div className="form__group">
        <div className="form__group__header">Video Metadata</div>
        <Fields
          names={videoEditFields}
          component={VideoEditDisplay}
          saveVideo={props.saveVideo}
          updateVideo={props.updateVideo}
          resetVideo={props.resetVideo}
          editable={props.editable}
          saveState={props.saveState}
          video={props.video}
          />
      </div>
    </div>
  )
};

export default reduxForm({
  form: 'VideoEdit',
  validate
})(VideoEdit)
