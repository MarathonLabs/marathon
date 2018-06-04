module.exports = (time) => {
  let ms = time % 1000;
  time = (time - ms) / 1000;
  const secs = time % 60;
  time = (time - secs) / 60;
  const mins = time % 60;

  const msLenth = ms.toString().length;
  if (msLenth === 2 ) ms = '0'+ms;
  if (msLenth === 1 ) ms = '00'+ms;

  return mins + ':' + secs + '.' + ms;
};
