import React from 'react';
import PropTypes from 'prop-types';
import DatePicker from '../FormFields/DatePicker';
import moment from 'moment';
import Icon from '../Icon';

const isFutureDate = date => date && moment(date).isAfter(moment());
const isSameOrAfter = (dateA, dateB) =>
  moment(dateA).isSameOrAfter(moment(dateB));
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

  componentWillReceiveProps(nextProps) {
    const embargo =
      nextProps.video.contentChangeDetails.embargo &&
      nextProps.video.contentChangeDetails.embargo.date;
    const scheduledLaunch =
      nextProps.video.contentChangeDetails.scheduledLaunch &&
      nextProps.video.contentChangeDetails.scheduledLaunch.date;
    this.setState({ selectedScheduleDate: scheduledLaunch || embargo });
    this.setState({ selectedEmbargoDate: embargo || scheduledLaunch });
  }

  validateDate = (date, actionType) => {
    if (!date) {
      this.setState({ invalidDateError: null });
      return;
    }

    const { video: { contentChangeDetails } } = this.props;
    const scheduledLaunch =
      contentChangeDetails &&
      contentChangeDetails.scheduledLaunch &&
      contentChangeDetails.scheduledLaunch.date;
    const embargo =
      contentChangeDetails &&
      contentChangeDetails.embargo &&
      contentChangeDetails.embargo.date;

    this.setState({ invalidDateError: null });

    if (!isFutureDate(date)) {
      this.setState({ invalidDateError: 'Date must be in the future!' });
    }

    if (
      actionType === 'schedule' &&
      embargo &&
      isFutureDate(date) &&
      !isSameOrAfter(date, embargo)
    ) {
      this.setState({
        invalidDateError: "Scheduled launch can't be earlier than embargo!"
      });
    }

    if (
      actionType === 'embargo' &&
      scheduledLaunch &&
      isFutureDate(date) &&
      isAfter(date, scheduledLaunch)
    ) {
      this.setState({
        invalidDateError: "Embargo can't be later than scheduled launch!"
      });
    }
  };

  onSelectOption = actionType => {
    const date =
      actionType === 'schedule'
        ? this.state.selectedScheduleDate
        : this.state.selectedEmbargoDate;
    this.validateDate(date, actionType);
    this.setState({
      showDatePicker: true,
      actionType,
      showScheduleOptions: false
    });
  };

  preventPublication = () => {
    const video = this.props.video;
    const impossiblyDistantDate = 16725225600000;
    this.props.saveVideo(
      Object.assign({}, video, {
        contentChangeDetails: Object.assign({}, video.contentChangeDetails, {
          embargo: Object.assign({}, video.contentChangeDetails.embargo, {
            date: impossiblyDistantDate
          })
        })
      })
    );
    this.setState({
      selectedEmbargoDate: impossiblyDistantDate,
      showScheduleOptions: false
    });
  };

  setDate = (date, actionType) => {
    if (!actionType) return;
    const key =
      actionType === 'schedule'
        ? 'selectedScheduleDate'
        : 'selectedEmbargoDate';

    this.setState({ [key]: date });
    this.validateDate(date, actionType);
  };

  saveDate = actionType => {
    const isSchedule = actionType === 'schedule';
    const key = isSchedule ? 'scheduledLaunch' : 'embargo';
    const selectedDate =
      actionType === 'schedule'
        ? 'selectedScheduleDate'
        : 'selectedEmbargoDate';
    const video = this.props.video;
    this.props.saveVideo(
      Object.assign({}, video, {
        contentChangeDetails: Object.assign({}, video.contentChangeDetails, {
          [key]: Object.assign({}, video.contentChangeDetails[key], {
            date: isSchedule
              ? this.state.selectedScheduleDate
              : this.state.selectedEmbargoDate
          })
        })
      })
    );
    this.setState({ showDatePicker: false, [selectedDate]: null });
  };

  removeDate = actionType => {
    const key = actionType === 'schedule' ? 'scheduledLaunch' : 'embargo';
    const selectedDate =
      actionType === 'schedule'
        ? 'selectedScheduleDate'
        : 'selectedEmbargoDate';
    const video = this.props.video;
    this.props.saveVideo(
      Object.assign({}, video, {
        contentChangeDetails: Object.assign({}, video.contentChangeDetails, {
          [key]: null
        })
      })
    );
    this.setState({ showDatePicker: false, [selectedDate]: null });
  };

  /* Render functions */

  renderScheduleOptions = (video, videoEditOpen, scheduledLaunch, embargo) => {
    const hasPreventedPublication = embargo && embargo > 16693689600000;
    return (
      <ul className="scheduleOptions">
        {!hasPreventedPublication && (
          <li>
            <button
              className="btn btn--list"
              onClick={() => this.onSelectOption('schedule')}
              disabled={!video || videoEditOpen}
            >
              {scheduledLaunch ? 'Edit scheduled date' : 'Schedule'}
            </button>
          </li>
        )}
        {!hasPreventedPublication && (
          <li>
            <button
              className="btn btn--list"
              onClick={() => this.onSelectOption('embargo')}
              disabled={!video || videoEditOpen}
            >
              {embargo ? 'Edit embargo' : 'Embargo until...'}
            </button>
          </li>
        )}
        {!embargo &&
          !scheduledLaunch && (
            <li>
              <button
                className="btn btn--list"
                onClick={() => this.preventPublication()}
                disabled={!video || videoEditOpen}
              >
                Prevent publication
              </button>
            </li>
          )}
        {hasPreventedPublication && (
          <li>
            <button
              className="btn btn--list"
              onClick={() => {
                this.removeDate('embargo');
                this.setState({ showScheduleOptions: false });
              }}
              disabled={!video || videoEditOpen}
            >
              Remove indefinite embargo
            </button>
          </li>
        )}
      </ul>
    );
  };

  renderAlert = invalidDateError =>
    invalidDateError && (
      <span className="topbar__alert">{invalidDateError}</span>
    );

  render() {
    const {
      video,
      video: { contentChangeDetails },
      videoEditOpen,
      hasPublishedVideoUsages
    } = this.props;
    const {
      selectedScheduleDate,
      selectedEmbargoDate,
      showScheduleOptions,
      actionType
    } = this.state;
    const showDatePicker = this.state.showDatePicker && !videoEditOpen;
    const invalidDateError = this.state.invalidDateError;
    const scheduledLaunch =
      contentChangeDetails &&
      contentChangeDetails.scheduledLaunch &&
      contentChangeDetails.scheduledLaunch.date;
    const embargo =
      contentChangeDetails &&
      contentChangeDetails.embargo &&
      contentChangeDetails.embargo.date;
    const hasPreventedPublication = embargo && embargo > 16693689600000;

    return (
      <div className="flex-container topbar__scheduler">
        {(scheduledLaunch || (embargo && !hasPreventedPublication)) &&
          !showDatePicker && (
            <div className="topbar__launch-label">
              <div>
                <span className="scheduledSummary--scheduledLaunch">
                  {'Scheduled: '}
                </span>
                {scheduledLaunch
                  ? moment(scheduledLaunch).format('Do MMM YYYY HH:mm')
                  : '-'}
              </div>
              <div>
                <span className="scheduledSummary--embargo">
                  {'Embargoed: '}
                </span>
                {embargo ? moment(embargo).format('Do MMM YYYY HH:mm') : '-'}
              </div>
            </div>
          )}
        {hasPreventedPublication && (
          <div className="topbar__launch-label">
            <span className="scheduledSummary--embargo">
              Embargoed indefinitely
            </span>
          </div>
        )}
        {showDatePicker && (
          <DatePicker
            editable={true}
            onUpdateField={date => this.setDate(date, actionType)}
            fieldValue={
              actionType === 'schedule'
                ? selectedScheduleDate
                : selectedEmbargoDate
            }
          />
        )}
        {showDatePicker && this.renderAlert(invalidDateError)}
        {!hasPublishedVideoUsages() &&
          !showDatePicker && (
            <div className="scheduleOptionsWrapper">
              <button
                className="btn btn--list"
                onClick={() =>
                  this.setState({
                    showScheduleOptions: !showScheduleOptions,
                    actionType: null
                  })
                }
              >
                <Icon icon="access_time" />
              </button>
              {showScheduleOptions &&
                this.renderScheduleOptions(
                  video,
                  videoEditOpen,
                  scheduledLaunch,
                  embargo
                )}
            </div>
          )}
        {showDatePicker && (
          <button
            className="button__secondary--confirm"
            onClick={() => this.saveDate(actionType)}
            disabled={
              invalidDateError ||
              ((actionType === 'schedule' && !selectedScheduleDate) ||
                (actionType === 'embargo' && !selectedEmbargoDate))
            }
          >
            Save
          </button>
        )}
        {((actionType === 'schedule' && scheduledLaunch) ||
          (actionType === 'embargo' && embargo)) &&
          showDatePicker && (
            <button
              className="button__secondary--remove"
              onClick={() => this.removeDate(actionType)}
            >
              Remove
            </button>
          )}
        {showDatePicker && (
          <button
            className="button__secondary--cancel"
            onClick={() =>
              this.setState({
                showDatePicker: false
              })
            }
          >
            Cancel
          </button>
        )}
      </div>
    );
  }
}
