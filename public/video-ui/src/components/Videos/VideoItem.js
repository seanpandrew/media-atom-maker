import React from 'react';
import { Link } from 'react-router';
import { findSmallestAssetAboveWidth } from '../../util/imageHelpers';
import moment from 'moment';
import Icon from '../Icon';
import ReactTooltip from 'react-tooltip';

export default class VideoItem extends React.Component {
  renderPill() {
    switch (this.props.video.status) {
      case 'Expired':
        return <span className="publish__label label__expired">Expired</span>;
      case 'Active':
        return (
          <span className="publish__label label__live label__frontpage__overlay">
            Active
          </span>
        );
      case 'No Video':
        return (
          <span className="publish__label label__frontpage__novideo label__frontpage__overlay">
            No Video
          </span>
        );
      default:
        return '';
    }
  }

  renderItemImage() {
    if (this.props.video.posterImage) {
      const image = findSmallestAssetAboveWidth(
        this.props.video.posterImage.assets
      );

      return <img src={image.file} alt={this.props.video.title} />;
    }

    return <div className="grid__image__placeholder">No Image</div>;
  }

  render() {
    const video = this.props.video;
    const scheduledLaunch =
      video.contentChangeDetails.scheduledLaunch &&
      video.contentChangeDetails.scheduledLaunch.date;
    const embargo =
      video.contentChangeDetails.embargo &&
      video.contentChangeDetails.embargo.date;
    const hasPreventedPublication = embargo && embargo > 16693689600000;
    return (
      <li className="grid__item">
        <Link className="grid__link" to={'/videos/' + video.id}>
          <div className="grid__info">
            <div className="grid__image sixteen-by-nine">
              {this.renderItemImage()}
            </div>
            <div className="grid__status__overlay">
              <ReactTooltip />
              {this.renderPill()}
              {embargo && (
                <span
                  data-tip={
                    hasPreventedPublication
                      ? 'This video has been embargoed indefinitely'
                      : `Embargoed until ${moment(embargo).format(
                          'Do MMM YYYY HH:mm'
                        )}`
                  }
                  className="publish__label label__frontpage__embargo label__frontpage__overlay"
                >
                  <Icon textClass="always-show" icon="not_interested">
                    {hasPreventedPublication
                      ? 'Publication prevented'
                      : moment(embargo).format('D MMM HH:mm')}
                  </Icon>
                </span>
              )}
              {scheduledLaunch && (
                <span
                  data-tip={`Scheduled to launch ${moment(
                    scheduledLaunch
                  ).format('Do MMM YYYY HH:mm')}`}
                  className="publish__label label__frontpage__scheduledLaunch label__frontpage__overlay"
                >
                  <Icon textClass="always-show" icon="access_time">
                    {moment(scheduledLaunch).format('D MMM HH:mm')}
                  </Icon>
                </span>
              )}
            </div>
            <div className="grid__item__footer">
              <span className="grid__item__title">{video.title}</span>
            </div>
          </div>
        </Link>
      </li>
    );
  }
}
