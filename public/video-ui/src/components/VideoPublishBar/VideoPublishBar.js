import React from 'react';
import {saveStateVals} from '../../constants/saveStateVals';
import {isVideoPublished} from '../../util/isVideoPublished';
import {hasUnpublishedChanges} from '../../util/hasUnpublishedChanges';
import Icon from '../../components/Icon';

export default class VideoPublishBar extends React.Component {

  videoIsCurrentlyPublishing() {
    return this.props.saveState.publishing === saveStateVals.inprogress;
  }

  videoHasUnpublishedChanges() {
    return hasUnpublishedChanges(this.props.video, this.props.publishedVideo);
  }

  renderUnpublishedNote() {
    return (
      <span className="publish-bar__message__block">
        <Icon icon="repeat"/>
        <span className="bar__message">This video atom has unpublished changes</span>
      </span>
    );
  }

  renderPublishButton() {
    return (<button
        type="button"
        className="btn"
        disabled={!this.videoHasUnpublishedChanges() || this.videoIsCurrentlyPublishing()}
        onClick={this.props.publishVideo}
      >
        Publish
      </button>
    );
  }

  renderPublishMessage() {
    return (
      <span className="bar__message publish-bar__message">Publishing...</span>
    );
  }

  renderVideoPublishedInfo() {
    if (isVideoPublished(this.props.publishedVideo)) {
      return <div className="publish__label label__live">Live</div>;
    }
    return <div className="publish__label label__draft">Draft</div>;
  }


  render() {

    if (!this.props.video) {
        return false;
    }

    if (this.videoIsCurrentlyPublishing()) {
      return (
        <div className="flex-container flex-grow publish-bar">
          {this.renderVideoPublishedInfo()}
          <div className="flex-spacer"></div>
          {this.renderPublishButton()}
          {this.renderPublishMessage()}
        </div>
      );
    }

    if (!this.videoHasUnpublishedChanges()) {
      return (
        <div className="flex-container flex-grow publish-bar">
          {this.renderVideoPublishedInfo()}
          <div className="flex-spacer"></div>
          {this.renderPublishButton()}
        </div>
      );
    }

    return (
      <div className="flex-container flex-grow publish-bar">
        {this.renderVideoPublishedInfo()}
        <div className="flex-spacer"></div>
        {this.renderUnpublishedNote()}
        {this.renderPublishButton()}
      </div>
    );
  }
}
