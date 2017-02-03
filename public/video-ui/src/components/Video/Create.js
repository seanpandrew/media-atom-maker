import React from 'react';
import VideoEdit from '../VideoEdit/VideoEdit';
import SaveButton from '../utils/SaveButton';
import {blankVideoData} from '../../constants/blankVideoData';

class VideoCreate extends React.Component {

  componentDidMount() {
    this.props.videoActions.populateEmptyVideo();
  }

  createVideo = () => {

    const videoWithStatus = Object.assign({}, this.props.video, {
      privacyStatus: 'Private'
    });

    this.props.videoActions.createVideo(videoWithStatus);
  };

  resetVideo = () => {
    this.props.videoActions.populateEmptyVideo();
  };

  updateVideo = (video) => {
    this.props.videoActions.updateVideo(video);
  };

  render () {
    return (
      <div className="container">
        <form className="form create-form">
          <h1>Create new video</h1>
          <VideoEdit
            video={this.props.video || blankVideoData}
            updateVideo={this.updateVideo}
            saveAndUpdateVideo={this.updateVideo}
            createMode
            editable
            saveState={this.props.saveState}
          />
          <SaveButton saveState={this.props.saveState} onSaveClick={this.createVideo} onResetClick={this.resetVideo} />
        </form>
      </div>
    );
  }
}

//REDUX CONNECTIONS
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';
import * as createVideo from '../../actions/VideoActions/createVideo';
import * as updateVideo from '../../actions/VideoActions/updateVideo';

function mapStateToProps(state) {
  return {
    video: state.video,
    saveState: state.saveState
  };
}

function mapDispatchToProps(dispatch) {
  return {
    videoActions: bindActionCreators(Object.assign({}, updateVideo, createVideo), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(VideoCreate);
