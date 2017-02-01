import React from 'react';
import { Field, reduxForm } from 'redux-form';
import validate from '../../constants/videoEditValidation';
import YoutubeKeywordsSelect from '../VideoEdit/formComponents/YoutubeKeywords';
import YoutubeChannelSelect from '../VideoEdit/formComponents/YoutubeChannel';
import YoutubeCategorySelect from '../VideoEdit/formComponents/YoutubeCategory';
import PrivacyStatusSelect from '../VideoEdit/formComponents/PrivacyStatus';

const YoutubeMetaData = (props) => {

    return (
      <div className="form__group">
        <Field
          name="youtubeCategory"
          type="select"
          component={YoutubeCategorySelect}
          video={props.video}
          saveAndUpdateVideo={props.saveAndUpdateVideo}
          editable={props.editable} />

        <Field
          name="youtubeChannel"
          type="select"
          component={YoutubeChannelSelect}
          video={props.video}
          editable={false} />

        <Field
          name="privacyStatus"
          type="text"
          component={PrivacyStatusSelect}
          video={props.video}
          saveAndUpdateVideo={props.saveAndUpdateVideo}
          editable={!props.disableStatusEditing && props.editable} />

        <Field
          name="youtubeKeywords"
          component={YoutubeKeywordsSelect}
          video={props.video}
          saveAndUpdateVideo={props.saveAndUpdateVideo}
          editable={props.editable} />
      </div>
    );
  };

export default reduxForm({
  form: 'YoutubeMetaData',
  validate
})(YoutubeMetaData);