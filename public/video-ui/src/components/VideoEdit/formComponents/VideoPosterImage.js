import React from 'react';

export default class VideoPosterImageEdit extends React.Component {

  constructor(props) {
    super(props);
  }


  onUpdatePosterImage = (e) => {
    let newData = Object.assign({}, this.props.video.data, {
      posterUrl: e.target.value
    });

    this.props.updateVideo(Object.assign({}, this.props.video, {
      data: newData
    }));
  };

  render () {
    return (
        <div className="form__row">
          <label className="form__label">Poster image</label>
          <input className="form__field" type="text" value={this.props.video.data.posterUrl || " "} onChange={this.onUpdatePosterImage} />
        </div>
    );
  }
}


