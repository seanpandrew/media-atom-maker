import React from 'react';
import PropTypes from 'prop-types';
import VideoItem from './VideoItem';
import { frontPageSize } from '../../constants/frontPageSize';

class Videos extends React.Component {
  static propTypes = {
    videos: PropTypes.array.isRequired
  };

  componentDidMount() {
    this.props.videoActions.getVideos(this.props.searchTerm, this.props.limit);
  }

  componentWillReceiveProps(newProps) {
    const oldSearch = this.props.searchTerm;
    const newSearch = newProps.searchTerm;

    if (oldSearch !== newSearch) {
      this.props.videoActions.getVideos(newSearch, this.props.limit);
    }
  }

  renderList() {
    if (this.props.videos.length) {
      return (
        <ul className="video-list">
          {this.renderListItems()}
        </ul>
      );
    } else {
      return <p className="grid__message">No videos found</p>;
    }
  }

  renderListItems() {
    return this.props.videos.map(video => (
      <VideoItem
        key={video.id}
        video={video}
        embeddedMode={this.props.config.embeddedMode}
      />
    ));
  }

  renderMoreLink() {
    if (this.props.videos.length === this.props.total) {
      return false;
    }

    const showMore = () => {
      this.props.videoActions.getVideos(
        this.props.searchTerm,
        this.props.limit + frontPageSize
      );
    };

    return (
      <div>
        <button className="btn video__load_more" onClick={showMore}>
          Load More
        </button>
      </div>
    );
  }

  render() {
    return (
      <div className="container">
        {this.renderList()}
        {this.renderMoreLink()}
      </div>
    );
  }
}

//REDUX CONNECTIONS
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';
import * as getVideos from '../../actions/VideoActions/getVideos';

function mapStateToProps(state) {
  return {
    videos: state.videos.entries,
    total: state.videos.total,
    limit: state.videos.limit,
    searchTerm: state.searchTerm,
    config: state.config
  };
}

function mapDispatchToProps(dispatch) {
  return {
    videoActions: bindActionCreators(Object.assign({}, getVideos), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(Videos);
