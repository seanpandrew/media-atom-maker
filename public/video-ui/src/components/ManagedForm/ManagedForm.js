import React from 'react';
import PropTypes from 'prop-types';

export class ManagedForm extends React.Component {
  static propTypes = {
    data: PropTypes.object,
    updateData: PropTypes.func.isRequired,
    children: PropTypes.oneOfType([
      PropTypes.element,
      PropTypes.arrayOf(PropTypes.element)
    ]),
    editable: PropTypes.bool,
    formName: PropTypes.string,
    formClass: PropTypes.string,
    updateErrors: PropTypes.func,
    updateWarnings: PropTypes.func
  };

  updateFormErrors = (fieldError, fieldName) => {
    if (this.props.updateErrors) {
      this.props.updateErrors({
        [this.props.formName]: { [fieldName]: fieldError }
      });
    }
  };

  updateWarnings = (hasFieldWarning, fieldName) => {
    if (this.props.updateWarnings) {
      this.props.updateWarnings({
        [fieldName]: hasFieldWarning
      });
    }
  };

  getFormClass = () => {
    if (
      React.Children
        .toArray(this.props.children)
        .some(child => child.type.componentType === 'managedSection')
    ) {
      return 'atom__section__form';
    }
    return '';
  };

  render() {
    const hydratedChildren = React.Children.map(this.props.children, child => {
      return React.cloneElement(child, {
        data: this.props.data,
        updateData: this.props.updateData,
        updateFormErrors: this.updateFormErrors,
        updateWarnings: this.updateWarnings,
        editable: this.props.editable
      });
    });

    return <div className={this.getFormClass()}>{hydratedChildren}</div>;
  }
}
