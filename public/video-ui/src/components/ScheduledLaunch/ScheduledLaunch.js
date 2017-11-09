import React from 'react';
import PropTypes from 'prop-types';
import DatePicker from '../FormFields/DatePicker';
import moment from 'moment';
import Icon from '../Icon';

const isFutureDate = (date) => date && moment(date).isAfter(moment());
const isSameOrAfter = (dateA, dateB) => moment(dateA).isSameOrAfter(moment(dateB));
const isAfter = (dateA, dateB) => moment(dateA).isAfter(moment(dateB));


export default class ScheduledLaunch extends React.Component {
  static propTypes = {
    video: PropTypes.object.isRequired,
    saveVideo: PropTypes.func.isRequired,
    videoEditOpen: PropTypes.bool.isRequired
  };

  state = {
    selectedScheduleDate: null,
    selectedEmbargoDate: null,
    showDatePicker: false,
    actionType: null,
    showScheduleButton: true,
    showScheduleOptions: false,
    invalidDateError: null
  };

  validateDate = (date, actionType) => {
    if (!date) {
      this.setState({ invalidDateError: null });
      return;
    }
    
    const { video: { contentChangeDetails } } = this.props;
    const scheduledLaunch = contentChangeDetails && contentChangeDetails.scheduledLaunch && contentChangeDetails.scheduledLaunch.date;
    const embargo = contentChangeDetails && contentChangeDetails.embargo && contentChangeDetails.embargo.date;

    this.setState({ invalidDateError: null });

    if (!isFutureDate(date)) {
      this.setState({ invalidDateError: "Date must be in the future!" });
    }

    if (actionType === 'schedule' && embargo && isFutureDate(date) && !isSameOrAfter(date, embargo)) {
      this.setState({ invalidDateError: "Scheduled launch can't be earlier than embargo!" });
    }

    if (actionType === 'embargo' && scheduledLaunch && isFutureDate(date) && isAfter(date, scheduledLaunch)) {
      this.setState({ invalidDateError: "Embargo can't be later than scheduled launch!" });
    }
  }

  onSelectOption = (actionType) => {
    const date = actionType === 'schedule' ? this.state.selectedScheduleDate : this.state.selectedEmbargoDate;
    this.validateDate(date, actionType);
    this.setState({ showDatePicker: true, actionType, showScheduleOptions: false });
  }

  setDate = (date, actionType) => {
    if (!actionType) return;
    const key = actionType === 'schedule' ? 'selectedScheduleDate' : 'selectedEmbargoDate';

    this.setState({ [key]: date });
    this.validateDate(date, actionType);
  }

  saveDate = (actionType) => {
    const isSchedule = actionType === 'schedule';
    const key = isSchedule ? 'scheduledLaunch' : 'embargo';
    const video = this.props.video;
    this.props.saveVideo(
      Object.assign({}, video, {
        contentChangeDetails: Object.assign({}, video.contentChangeDetails, {
          [key]: Object.assign({}, video.contentChangeDetails[key], {
            date: isSchedule ? this.state.selectedScheduleDate : this.state.selectedEmbargoDate
          })
        })
      })
    );
    this.setState({ showDatePicker: false });
  }

  removeDate = (actionType) => {
    const key = actionType === 'schedule' ? 'scheduledLaunch' : 'embargo';
    const video = this.props.video;
    this.props.saveVideo(
      Object.assign({}, video, {
        contentChangeDetails: Object.assign({}, video.contentChangeDetails, {
          [key]: null
        })
      })
    );
    this.setState({ showDatePicker: false });
  }

  /* Render functions */

  renderScheduleOptions = (video, videoEditOpen, scheduledLaunch, embargo) =>
    <ul className="scheduleOptions">
      <li>
        <button
          className="btn btn--list"
          onClick={() => this.onSelectOption('schedule')}
          disabled={!video || videoEditOpen}
        >
          {scheduledLaunch ? 'Edit scheduled date' : 'Schedule'}
        </button>
      </li>
      <li>
        <button
          className="btn btn--list"
          onClick={() => this.onSelectOption('embargo')}
          disabled={!video || videoEditOpen}
        >
          {embargo ? 'Edit embargo' : 'Embargo until...'}
        </button>
      </li>
    </ul>

  renderAlert = (invalidDateError) => invalidDateError && <span className="topbar__alert">{invalidDateError}</span>;

  render() {
    const { video, video: { contentChangeDetails }, videoEditOpen } = this.props;
    const { selectedScheduleDate, selectedEmbargoDate, showScheduleOptions, actionType } = this.state;
    const showDatePicker = this.state.showDatePicker && !videoEditOpen;
    const invalidDateError = this.state.invalidDateError;
    const scheduledLaunch = contentChangeDetails && contentChangeDetails.scheduledLaunch && contentChangeDetails.scheduledLaunch.date;
    const embargo = contentChangeDetails && contentChangeDetails.embargo && contentChangeDetails.embargo.date;
    
    return (
      <div className="flex-container topbar__scheduler">
        {
          (scheduledLaunch || embargo) && !showDatePicker &&
          <div className="topbar__launch-label">
            <div><strong>Scheduled:</strong> {scheduledLaunch ? moment(scheduledLaunch).format('Do MMM YYYY HH:mm') : '-'}</div>
            <div><strong>Embargoed:</strong> {embargo ? moment(embargo).format('Do MMM YYYY HH:mm') : '-'}</div>
          </div>
        }
        {
          showDatePicker &&
          <DatePicker
            editable={true}
            onUpdateField={(date) => this.setDate(date, actionType)}
            fieldValue={actionType === 'schedule' ? selectedScheduleDate : selectedEmbargoDate}
            placeholder="Set a date..."
          />
        }
        {showDatePicker && this.renderAlert(invalidDateError)}
        {
          !showDatePicker &&
          <div className="scheduleOptionsWrapper">
            <button className="btn" onClick={() => this.setState({ showScheduleOptions: !showScheduleOptions, actionType: null })}>
              <Icon icon="access_time"></Icon>
            </button>
            {showScheduleOptions && this.renderScheduleOptions(video, videoEditOpen, scheduledLaunch, embargo)}
          </div>
        }
        {
          showDatePicker &&
          <button
            className="button__secondary--confirm"
            onClick={() => this.saveDate(actionType)}
            disabled={invalidDateError || (actionType === 'schedule' && !selectedScheduleDate || actionType === 'embargo' && !selectedEmbargoDate)}
          >
            Save
          </button>
        }
        {
          (actionType === 'schedule' && scheduledLaunch || actionType === 'embargo' && embargo) && showDatePicker &&
          <button className="button__secondary--remove" onClick={() => this.removeDate(actionType)}>
            Remove
          </button>
        }
        {
          showDatePicker &&
          <button className="button__secondary--cancel" onClick={() => this.setState({ showDatePicker: false })}>
            Cancel
          </button>
        }
      </div>
    );
  }
}
