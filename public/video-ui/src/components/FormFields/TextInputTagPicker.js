import React from 'react';
import { PropTypes } from 'prop-types';
import { keyCodes } from '../../constants/keyCodes';
import UserActions from '../../constants/UserActions';
import TagTypes from '../../constants/TagTypes';
import CapiSearch from '../CapiSearch/CapiSearch';
import removeStringTagDuplicates from '../../util/removeStringTagDuplicates';
import removeTagDuplicates from '../../util/removeTagDuplicates';

export default class TextInputTagPicker extends React.Component {

  static propTypes = {
    tagValue: PropTypes.array.isRequired,
    onUpdate: PropTypes.func.isRequired,
    fetchTags: PropTypes.func.isRequired,
    removeFn: PropTypes.func.isRequired,
    onUpdate: PropTypes.func.isRequired,
    capiTags: PropTypes.array.isRequired,
    tagsToVisible: PropTypes.func.isRequired,
    showTags: PropTypes.bool.isRequired,
    hideTagResults: PropTypes.func.isRequired,
    selectedTagIndex: PropTypes.number,
    inputClearCount: PropTypes.number.isRequired,
    disableCapiTags: PropTypes.bool,
    tagType: PropTypes.string,
    fieldName: PropTypes.string
  }

  state = {
    inputString: '',
    lastAction: UserActions.other,
  };

  componentWillReceiveProps(nextProps) {
    if (this.props.inputClearCount !== nextProps.inputClearCount) {
      this.setState({
        inputString: '',
      });
    }
  }

  selectNewTag = (newFieldValue) => {

      this.setState({
        inputString: ''
      });

      this.props.onUpdate(newFieldValue);
  }

  getYoutubeInputValue = () => {
    if (
      this.props.tagValue.every(value => {
        return value.id !== this.state.inputString;
      })
    ) {
      return {
        id: this.state.inputString,
        webTitle: this.state.inputString
      };
    }
    return '';
  };

  getLastTextInputElement = () => {
    const lastElement = this.props.tagValue[this.props.tagValue.length - 1];
    if (typeof lastElement === 'string') {
      return lastElement;
    }
    return null;
  }

  updateInput = e => {
      // If the user did not add new text input, we update the tag search
    if (this.state.lastAction === UserActions.delete) {
      const length = this.props.tagValue.length;
      const lastInput = this.props.tagValue[length - 1];

      this.setState({
        inputString: lastInput,
        lastAction: UserActions.other
      });

      const newValue = this.props.tagValue.slice(
        0,
        this.props.tagValue.length - 1
      );
      this.props.onUpdate(newValue);
    } else {

      if (!this.props.disableCapiTags) {

        const allWords = e.target.value.split(' ').reverse().filter(word => {
          return word.length !== 0
        });
        let searchTextElems = [];
        for (var i = 0; i < allWords.length; i++) {
          const word = allWords[i];
          if (word[0].toUpperCase() === word[0]) {
            searchTextElems.push(word);
          } else {
            break;
          }
        }

        const searchText = searchTextElems.reverse().join(' ');

        this.props.fetchTags(searchText);

      }

      if (this.props.selectedTagIndex === null) {
        //input string here should be last element
        const newInput = e.target.value;

        const parsedNewInput = this.props.tagType === TagTypes.youtube ? this.getYoutubeInputValue() : newInput

        let newFieldValue;

        if (this.props.tagValue.length === 0) {
          newFieldValue = [parsedNewInput];
        } else {
          //but if tag? fix
          const lastValue = this.props.tagValue[this.props.tagValue.length - 1];
          const oldValues = typeof lastValue === 'string' ? this.props.tagValue.slice(0, -1) : this.props.tagValue;
          newFieldValue = oldValues.concat([parsedNewInput]);
        }

        this.props.onUpdate(newFieldValue);
      }
    }
  };

  processTagInput = e => {

    if (e.keyCode === keyCodes.backspace) {
      if (this.state.inputString.length === 0) {
        const lastInput = this.props.tagValue[this.props.tagValue.length - 1];

        if (typeof lastInput === 'string') {
          //User is trying to delete a string input
          this.setState(
            {
              lastAction: UserActions.delete
            },
            () => {
              this.updateInput();
            }
          );
        }
      }
    } else {
      this.setState({
        lastAction: UserActions.other
      });
    }
  };

  renderValue = (field, i, isLastInput = false) => {

    if (field.id) {
      return (
        <span
          className="form__field--multiselect__value form__field__tag__remove"
          key={`${field.id}-${i}`}
          onClick={tag => this.props.removeFn(field)}
        >
          {field.webTitle}{' '}
        </span>
      );
    }
    return (
      <span
        className={ !isLastInput ? "form__field--multistring__value" : "form__field--multistring__last"}
        key={`${field.id}-${i}`}
      >
        {' '}{field}{' '}
      </span>
    );
  };

  renderTextInputElement(lastElement) {
    //TODO split the value displayed at the end

    return (
      <span className="form__field__tag--container">
        {lastElement && (typeof lastElement !== 'string') && this.renderValue(lastElement, 0, true)}
        <input
          type="text"
          className="form__field__tag--input"
          id={this.props.fieldName}
          onKeyDown={this.processTagInput}
r         onChange={this.updateInput}
          value={typeof lastElement === 'string' ? lastElement : ''}
        />
      </span>
    );
  }

  renderInputElements() {

    const valueLength = this.props.tagValue.length;
    const lastElement = !valueLength || valueLength === 0
      ? null
      : this.props.tagValue[valueLength - 1];

    return (
      <div className="form__field__tag--selector">
        {valueLength
          ? this.props.tagValue.map((value, i) => {
              if (i < valueLength - 1) {
                return this.renderValue(value, i);
              }
            })
          : ''}

        {this.renderTextInputElement(lastElement)}

      </div>
    );
  }

  render() {
    return (
      <div>

        {this.renderInputElements()}

        <CapiSearch
          capiTags={this.props.capiTags}
          showTags={this.props.showTags}
          tagsToVisible={this.props.tagsToVisible}
          selectNewTag={this.selectNewTag}
          tagValue={this.props.tagValue}
          removeDupes={removeStringTagDuplicates}
          selectedTagIndex={this.props.selectedTagIndex}
        />

      </div>
    );
  }
};
