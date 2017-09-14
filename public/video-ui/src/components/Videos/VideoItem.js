import React from 'react';
import { Link } from 'react-router';
import { findSmallestAssetAboveWidth } from '../../util/imageHelpers';

export default class VideoItem extends React.Component {
  renderActions() {
    switch (this.props.video.status) {
      case 'Expired':
        return <div className="publish__label label__expired">Expired</div>;
      case 'Active':
        if (this.props.embeddedMode) {
          return (
            <span>hasdsa</span>
          );
        } else {
          return (
            <span/>
          );
        }
      default:
        return (
          <span className="publish__label label__frontpage__novideo label__frontpage__overlay">
            No Video
          </span>
        );
    }
  }

  renderItemImage() {
    if (!this.props.video.posterImage) {
      return (
        <div className="video-list__item__image--content">
          No Image
        </div>
      );
    }

    const image = findSmallestAssetAboveWidth(
      this.props.video.posterImage.assets
    );

    return (
      <img
        className="video-list__item__image--content"
        src={image.file}
        alt={this.props.video.title}
      />
    );
  }

  render() {
    return (
      <li className="video-list__item">
        <Link className="grid__link_" to={'/videos/' + this.props.video.id}>
          <div className="video-list__item__image">
            {this.renderItemImage()}
          </div>
          <div className="video-list__item__title">
            {this.props.video.title}
          </div>
        </Link>
        <div className="video-list__item__actions">
          {this.renderActions()}
        </div>
      </li>
    );
  }
}
