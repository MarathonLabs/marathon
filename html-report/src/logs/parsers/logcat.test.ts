import { describe, it, expect } from 'vitest';
import { logcatParser } from './logcat';

describe('logcatParser', () => {
  describe('marathon-emitted format (adb logcat -v long, re-serialized)', () => {
    const line = "07-25 16:12:05.006 12345-12346/? I/AndroidRuntime: bootstrap ok";

    it('parses timestamp, pid, tid, level, tag, message', () => {
      const entry = logcatParser.parse(line, 0);
      expect(entry.timestamp).toBe('07-25 16:12:05.006');
      expect(entry.pid).toBe('12345');
      expect(entry.tid).toBe('12346');
      expect(entry.level).toBe('I');
      expect(entry.tag).toBe('AndroidRuntime');
      expect(entry.message).toBe('bootstrap ok');
    });

    it('scores well in the detector', () => {
      const score = logcatParser.detect(Array(10).fill(line));
      expect(score).toBe(1);
    });

    it('handles appName populated (not just "?")', () => {
      const populated = "07-25 16:12:05.006 12345-12346/com.aetna.app D/tag: hello";
      const entry = logcatParser.parse(populated, 0);
      expect(entry.tag).toBe('tag');
      expect(entry.message).toBe('hello');
    });

    it('handles hex thread id (some Android versions do this)', () => {
      const hex = "07-25 16:12:05.006 12345-a1f/? W/tag: warn";
      const entry = logcatParser.parse(hex, 0);
      expect(entry.tid).toBe('a1f');
      expect(entry.level).toBe('W');
    });
  });

  describe('threadtime format (adb logcat -v threadtime)', () => {
    const line = "07-25 16:12:05.006  12345 12346 I AndroidRuntime: bootstrap ok";

    it('parses timestamp, pid, tid, level, tag, message', () => {
      const entry = logcatParser.parse(line, 0);
      expect(entry.timestamp).toBe('07-25 16:12:05.006');
      expect(entry.pid).toBe('12345');
      expect(entry.tid).toBe('12346');
      expect(entry.level).toBe('I');
      expect(entry.tag).toBe('AndroidRuntime');
      expect(entry.message).toBe('bootstrap ok');
    });
  });

  describe('unparseable lines', () => {
    it('keeps raw line in message', () => {
      const entry = logcatParser.parse('some free-form text', 0);
      expect(entry.message).toBe('some free-form text');
      expect(entry.timestamp).toBeUndefined();
      expect(entry.tag).toBeUndefined();
    });
  });
});
