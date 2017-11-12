import { pandaReqwest } from './pandaReqwest';
import { errorDetails } from '../util/errorDetails';

// See http://andrewhfarmer.com/aws-sdk-with-webpack/ for why this is strange
import 'aws-sdk/dist/aws-sdk';
const AWS = window.AWS;

export function getUploads(atomId) {
  return pandaReqwest({
    url: `/api2/uploads?atomId=${atomId}`
  });
}

export function createUpload(atomId, file, selfHost) {
  return pandaReqwest({
    url: `/api2/uploads`,
    method: 'post',
    data: {
      atomId: atomId,
      filename: file.name,
      size: file.size,
      selfHost: selfHost
    }
  });
}

function getCredentials({ id, key }) {
  const payload = key ? { uploadId: id, key } : { atomId: id };

  return pandaReqwest({
    url: `/api2/uploads/credentials`,
    method: 'post',
    data: payload
  });
}

function getS3({ temporaryAccessId, temporarySecretKey, sessionToken, bucket, region }) {
  const awsCredentials = new AWS.Credentials(
    temporaryAccessId,
    temporarySecretKey,
    sessionToken
  );

  return new AWS.S3({
    apiVersion: '2006-03-01',
    credentials: awsCredentials,
    params: { Bucket: bucket },
    region: region
  });
}

function uploadPart(upload, part, file, progressFn) {
  const slice = file.slice(part.start, part.end);

  return getCredentials({id: upload.id, key: part.key}).then(credentials => {
    const s3 = getS3(credentials);

    const params = {
      Key: credentials.key,
      Body: slice,
      ACL: 'private',
      Metadata: { original: file.name }
    };
    const request = s3.upload(params);

    request.on('httpUploadProgress', event => {
      progressFn(part.start + event.loaded);
    });

    return request.promise();
  });
}

export function uploadParts(upload, parts, file, progressFn) {
  return new Promise((resolve, reject) => {
    function uploadPartRecursive(parts) {
      if (parts.length === 0) {
        resolve(true);
      } else {
        const part = parts[0];
        const result = uploadPart(upload, part, file, progressFn);

        result
          .then(() => {
            uploadPartRecursive(parts.slice(1));
          })
          .catch(err => {
            reject(errorDetails(err));
          });
      }
    }

    uploadPartRecursive(parts);
  });
}

export function uploadPacFile({id, file}) {
  return getCredentials({id}).then(credentials => {
    const s3 = getS3(credentials);

    const params = {
      Key: credentials.key,
      Body: file,
      ACL: 'private',
      Metadata: {
        original: file.name
      }
    };

    const request = s3.upload(params);

    return request.promise();
  });
}
