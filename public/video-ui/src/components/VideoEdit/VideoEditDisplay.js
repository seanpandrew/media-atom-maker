import React from 'react';
import VideoTitleEdit from './formComponents/VideoTitle';
import VideoCategorySelect from './formComponents/VideoCategory';
import VideoDurationEdit from './formComponents/VideoDuration';
import FormFieldSaveWrapper from '../FormFields/FormFieldSaveWrapper';
import VideoPosterEdit from './formComponents/VideoPoster';
import YoutubeCategorySelect from './formComponents/YoutubeCategory';
import YoutubeKeywordsSelect from './formComponents/YoutubeKeywords';
import YoutubeChannelSelect from './formComponents/YoutubeChannel';
import PrivacyStatusSelect from './formComponents/PrivacyStatus';
import ContentFlags from './formComponents/ContentFlags';

export default class VideoEditDisplay extends React.Component {

  render() {
    console.log(this.props);
    return (
      <div>
        <FormFieldSaveWrapper
          saveVideo={this.props.saveVideo}
          resetVideo={this.props.resetVideo}
          editable={this.props.editable}
          saveState={this.props.saveState}>
          <VideoTitleEdit
            video={this.props.video}
            input={this.props.title.input}
            meta={this.props.title.meta}
            updateVideo={this.props.updateVideo}
            editable={this.props.editable} />
        </FormFieldSaveWrapper>

        <FormFieldSaveWrapper
          saveVideo={this.props.saveVideo}
          resetVideo={this.props.resetVideo}
          editable={this.props.editable}
          saveState={this.props.saveState}>
          <VideoCategorySelect
            video={this.props.video}
            input={this.props.title.input}
            meta={this.props.title.meta}
            updateVideo={this.props.updateVideo}
            editable={this.props.editable} />
        </FormFieldSaveWrapper>

      </div>
    );
  }
}

