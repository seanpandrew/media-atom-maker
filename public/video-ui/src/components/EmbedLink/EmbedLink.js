import React from 'react';
import PropTypes from 'prop-types';
import Clipboard from 'clipboard';
import { isVideoPublished } from '../../util/isVideoPublished';
import Icon from '../Icon';

export default class EmbedLink extends React.Component {
  static propTypes = {
    capiBaseUrl: PropTypes.string.isRequired,
    video: PropTypes.object.isRequired,
    publishedVideo: PropTypes.object
  };

  get capiUrl() {
    return `${this.props.capiBaseUrl}/atom/media/${this.props.video.id}`;
  }

  onCopyClick(e) {
    const capiUrl = this.capiUrl;

    const clipboard = new Clipboard(e.target, {
      text: () => capiUrl
    });

    clipboard.on('success', function() {
      console.log(arguments);
    });
  }

  render() {
    if (!isVideoPublished(this.props.publishedVideo)) {
      return (
        <section>Not published</section>
      );
    }

    return (
      <section>
        <span id="foo">{this.capiUrl}</span>
        <button className="btn"
                data-clipboard-action="copy"
                data-clipboard-target="#foo"
                onClick={(e) => this.onCopyClick(e)}>
          <Icon icon="content_copy">Copy</Icon>
        </button>
      </section>
    );
  }
}
