import React from 'react';
import VideoTitleEdit from './formComponents/VideoTitle';
import VideoCategorySelect from './formComponents/VideoCategory';
import VideoDurationEdit from './formComponents/VideoDuration';
import FormFieldSaveWrapper from '../FormFields/FormFieldSaveWrapper';
import VideoPosterEdit from './formComponents/VideoPoster';
import YoutubeCategorySelect from './formComponents/YoutubeCategory';
import validate from '../../constants/videoEditValidation';
import { Field, reduxForm } from 'redux-form';

const VideoEdit = (props) => {
    return (
        <div>

          <FormFieldSaveWrapper {...props}>
            <Field
              name="title"
              type="text"
              component={VideoTitleEdit}
              {...props} />
          </FormFieldSaveWrapper>

          <FormFieldSaveWrapper {...props}>
            <Field
              name="category"
              type="select"
              component={VideoCategorySelect}
              {...props} />
          </FormFieldSaveWrapper>

          <FormFieldSaveWrapper {...props}>
            <Field
              name="duration"
              type="number"
              component={VideoDurationEdit}
              {...props} />
          </FormFieldSaveWrapper>

          <Field name="posterImage" component={VideoPosterEdit} {...props} />

          <FormFieldSaveWrapper {...props}>
            <Field
              name="youtube-category"
              type="select"
              component={YoutubeCategorySelect}
              {...props} />
          </FormFieldSaveWrapper>
        </div>
    )
};

export default reduxForm({
  form: 'VideoEdit',
  validate
})(VideoEdit)
