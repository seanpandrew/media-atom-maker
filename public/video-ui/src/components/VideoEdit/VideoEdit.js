import React from 'react';
import VideoTitleEdit from './formComponents/VideoTitle';
import VideoCategorySelect from './formComponents/VideoCategory';
import VideoDurationEdit from './formComponents/VideoDuration';
import FormFieldSaveWrapper from '../FormFields/FormFieldSaveWrapper';
import VideoPosterEdit from './formComponents/VideoPoster';
import validate from '../../constants/videoEditValidation';
import { Field, reduxForm } from 'redux-form';

const VideoEdit = (props) => {
    return (
        <div>
          <Field name="title" type="text" component={VideoTitleEdit} {...props} />
          <Field name="category" type="text" component={VideoCategorySelect} {...props} />
          <Field name="duration" type="number" component={VideoDurationEdit} {...props} />
          <Field name="posterImage" component={VideoPosterEdit} {...props} />

          <FormFieldSaveWrapper {...props}>
            <Field name="title" type="text" component={VideoTitleEdit} {...props} />
          </FormFieldSaveWrapper>

        </div>
    )
};

export default reduxForm({
  form: 'VideoEdit',
  validate
})(VideoEdit)
