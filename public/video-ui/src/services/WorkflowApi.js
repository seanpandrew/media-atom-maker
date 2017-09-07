import { pandaReqwest } from './pandaReqwest';
import { getStore } from '../util/storeAccessor';
import getProductionOffice from '../util/getProductionOffice';
import moment from 'moment';

export default class WorkflowApi {
  static get workflowUrl() {
    return getStore().getState().config.workflowUrl;
  }

  static workflowItemLink() {
    //TODO link to atom in Workflow
    return `${WorkflowApi.workflowUrl}/dashboard?atom-type=media`;
  }

  static getSections() {
    return pandaReqwest({
      url: `${WorkflowApi.workflowUrl}/api/sections`,
      crossOrigin: true,
      withCredentials: true
    }).then(response => {
      return response.data
        .map(section => Object.assign({}, section, { title: section.name }))
        .sort((a, b) => {
          if (a.title.toLowerCase() < b.title.toLowerCase()) return -1;
          if (a.title.toLowerCase() > b.title.toLowerCase()) return 1;
          return 0;
        });
    });
  }

  static getAtomInWorkflow({ video }) {
    return pandaReqwest({
      url: `${WorkflowApi.workflowUrl}/api/atom/${video.id}`,
      crossOrigin: true,
      withCredentials: true
    }).then(response => response.data);
  }

  static _getTrackInWorkflowPayload({ video, status, section, scheduledLaunchDate }) {
    const prodOffice = getProductionOffice();

    const core = {
      contentType: 'media',
      editorId: video.id,
      title: video.title,
      priority: 0,
      needsLegal: 'NA',
      section,
      status,
      prodOffice
    };

    if (!scheduledLaunchDate) {
      return core;
    }

    const momentLaunchDate = moment(scheduledLaunchDate);

    return Object.assign({}, core, {
      scheduledLaunchDate: momentLaunchDate,
      note: `Please create a Video page, launching ${momentLaunchDate.format("DD MMM YYYY HH:mm")}`
    });
  }

  static trackInWorkflow({ video, status, section, scheduledLaunchDate }) {
    const payload = WorkflowApi._getTrackInWorkflowPayload({
      video,
      status,
      section,
      scheduledLaunchDate
    });

    return pandaReqwest({
      method: 'POST',
      url: `${WorkflowApi.workflowUrl}/api/stubs`,
      data: payload,
      crossOrigin: true,
      withCredentials: true
    });
  }
}
