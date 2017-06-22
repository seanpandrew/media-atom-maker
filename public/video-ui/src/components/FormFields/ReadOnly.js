import React from 'react';

export default class ReadOnly extends React.Component {
  render() {
    return (
      <div>
        <p className="details-list__title">{this.props.fieldName}</p>
        <p
          className={
            'details-list__field ' +
              (this.props.displayPlaceholder(
                this.props.placeholder,
                this.props.fieldValue
              )
                ? 'details-list__empty'
                : '')
          }
        >
          {this.props.fieldValue}
        </p>
      </div>
    );
  }
}
