export function addQuery(uri: string, params: { [key: string]: any } | undefined): string {
  if (params !== undefined) {
    return uri + objectToQueryString(params);
  }
  return uri;
}

function objectToQueryString(obj: { [key: string]: any }): string {
  const queryString = Object.keys(obj)
    .filter(key => obj[key] !== undefined)
    .map(key => `${encodeURIComponent(key)}=${encodeURIComponent(obj[key])}`)
    .join('&');
  return `?${queryString}`;
}

export function parseURITemplate(uri: string, params: { [key: string]: string }): string {
  let result = uri;

  for (const key in params) {
    if (Object.prototype.hasOwnProperty.call(params, key)) {
      const pattern = `{${key}}`;
      const value = encodeURIComponent(params[key]);

      result = result.replace(new RegExp(pattern, 'g'), value);
    }
  }

  return result;
}
