import React from 'react';
import moment from 'moment';
import Icon from '../Icon';
import { YouTubeEmbed } from '../utils/YouTubeEmbed';
import { VideoEmbed } from '../utils/VideoEmbed';

function presenceInitials(email) {
  const emailParts = email.split('@');
  const names = [];

  if (emailParts.length < 2) {
    names.push(emailParts[0]);
  } else {
    const nameParts = emailParts[0].split('.');
    names.push(...nameParts.slice(0, 2));
  }

  const initials = names.map(name => name.toUpperCase()[0]);

  return initials.join('');
}

function AssetControls({ user, children, selectAsset }) {
  const activateButton = selectAsset
    ? <button className="btn upload__activate-btn" onClick={selectAsset}>
        Activate
      </button>
    : false;

  const initials = user ? presenceInitials(user) : false;
  const userCircle = initials
    ? <ul className="presence-list">
        <li className="presence-list__user" title={user}>
          {initials}
        </li>
      </ul>
    : false;

  return (
    <div className="upload__actions">
      {children}
      <div className="upload__right">
        {userCircle}
        {activateButton}
      </div>
    </div>
  );
}

function AssetInfo({ info, timestamp }) {
  const startDate = timestamp
    ? moment(timestamp).format('YYYY/MM/DD HH:mm:ss')
    : false;

  return (
    <div className="upload__left">
      <div className="upload__info" title={info}>
        {info}
      </div>
      <div>
        <small>{startDate}</small>
      </div>
    </div>
  );
}

function AssetDisplay({ id, active, sources }) {
  const linkProps = id
    ? {
        className: 'upload__link',
        href: `https://www.youtube.com/watch?v=${id}`,
        target: '_blank',
        rel: 'noopener noreferrer'
      }
    : false;

  return (
    <div className="upload">
      {id ? <YouTubeEmbed id={id} /> : <VideoEmbed sources={sources} />}
      {linkProps
        ? <a {...linkProps}>
            <Icon icon="open_in_new" className="icon__assets" />
          </a>
        : false}
      {active
        ? <div className="grid__status__overlay">
            <span className="publish__label label__live label__frontpage__overlay">
              Active
            </span>
          </div>
        : false}
    </div>
  );
}

function AssetProgress({ failed, current, total }) {
  if (failed) {
    return (
      <div className="upload">
        <p>
          <strong>Upload Failed</strong>
        </p>
      </div>
    );
  }

  return total !== undefined && current !== undefined
    ? <progress className="progress" value={current} max={total} />
    : <span className="loader" />;
}

export function Asset({ upload, active, selectAsset }) {
  const { asset, metadata, processing } = upload;
  const user = metadata ? metadata.user : false;

  if (processing) {
    return (
      <div className="grid__item">
        <div className="upload">
          <AssetProgress {...processing} />
        </div>
        <div className="grid__item__footer">
          <AssetControls user={user}>
            <AssetInfo info={processing.status} />
          </AssetControls>
        </div>
      </div>
    );
  }

  if (asset) {
    const info = metadata ? metadata.originalFilename : `Version ${upload.id}`;
    const timestamp = metadata ? metadata.startTimestamp : false;

    return (
      <div className="grid__item">
        <AssetDisplay active={active} id={asset.id} sources={asset.sources} />
        <div className="grid__item__footer">
          <AssetControls user={user} selectAsset={active ? false : selectAsset}>
            <AssetInfo info={info} timestamp={timestamp} />
          </AssetControls>
        </div>
      </div>
    );
  }

  return false;
}
