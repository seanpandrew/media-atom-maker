import React from 'react';
import { saveStateVals } from '../../constants/saveStateVals';
import { isVideoPublished } from '../../util/isVideoPublished';
import { hasUnpublishedChanges } from '../../util/hasUnpublishedChanges';
import { getStore } from '../../util/storeAccessor';

export default class VideoPublishBar extends React.Component {
  videoIsCurrentlyPublishing() {
    return this.props.saveState.publishing === saveStateVals.inprogress;
  }

  videoHasUnpublishedChanges() {
    return hasUnpublishedChanges(
      this.props.video,
      this.props.publishedVideo,
      this.props.editableFields
    );
  }

  isPublishingDisabled() {
    return (
      this.videoIsCurrentlyPublishing() ||
      this.props.videoEditOpen ||
      !this.videoHasUnpublishedChanges()
    );
  }

  publishVideo = () => {

    this.props.publishVideo();
  };

  renderPublishButtonText() {
    if (this.videoIsCurrentlyPublishing()) {
      return <span>Publishing</span>;
    }

    if (
      isVideoPublished(this.props.publishedVideo) &&
      !this.videoHasUnpublishedChanges()
    ) {
      return <span>Published</span>;
    }

    return <span>Publish</span>;
  }

  renderPublishButton() {
    return (
      <button
        type="button"
        className="btn"
        disabled={this.isPublishingDisabled()}
        onClick={this.publishVideo}
      >
        {this.renderPublishButtonText()}
      </button>
    );
  }

  render() {
    if (!this.props.video) {
      return false;
    }

    return (
      <div className="flex-container publish-bar">
        {this.renderPublishButton()}
      </div>
    );
  }
}
