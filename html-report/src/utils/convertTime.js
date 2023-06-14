module.exports = (time) => {
  let ms = time % 1000;
  time = (time - ms) / 1000;
  const secs = time % 60;
  time = (time - secs) / 60;
  const mins = time % 60;
  time = (time - mins) / 60;
  const hrs = time

  const msLenth = ms.toString().length;
  if (msLenth === 2 ) ms = '0'+ms;
  if (msLenth === 1 ) ms = '00'+ms;
  
  if (hrs > 0) {
      return hrs + ":" + mins + ':' + secs + '.' + ms;
  } else {
      return mins + ':' + secs + '.' + ms;
  }
};
