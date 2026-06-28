package me.nghiadark.antideepslateesp.protocol;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class SectionModifier {

    public static byte[] clearSectionsBelow(byte[] sections, int worldMinY, int worldMaxY, int hideBelowY) {
        int sectionCount = (worldMaxY - worldMinY) >> 4;
        int lastTargetSection = (hideBelowY - worldMinY) >> 4;

        if (lastTargetSection < 0 || sectionCount == 0) return sections;

        ByteArrayInputStream in = new ByteArrayInputStream(sections);
        ByteArrayOutputStream out = new ByteArrayOutputStream(sections.length);

        for (int i = 0; i < sectionCount; i++) {
            boolean shouldClear = i <= lastTargetSection;

            if (shouldClear) {
                writeEmptySection(out);
                skipSection(in);
            } else {
                copySection(in, out);
            }
        }

        return out.toByteArray();
    }

    private static void writeEmptySection(ByteArrayOutputStream out) {
        out.write(0);
        writeVarInt(out, 0);
        out.write(0);
        writeVarInt(out, 1);
    }

    private static void skipSection(ByteArrayInputStream in) {
        skipPalettedContainer(in);
        skipPalettedContainer(in);
    }

    private static void copySection(ByteArrayInputStream in, ByteArrayOutputStream out) {
        in.mark(8192);
        int blockSize = measurePalettedContainer(in);
        int bioSize = measurePalettedContainer(in);
        int totalSize = blockSize + bioSize;
        in.reset();

        byte[] buf = new byte[Math.min(totalSize, 4096)];
        int remaining = totalSize;
        while (remaining > 0) {
            int read = in.read(buf, 0, Math.min(buf.length, remaining));
            if (read < 0) break;
            out.write(buf, 0, read);
            remaining -= read;
        }
    }

    private static void skipPalettedContainer(ByteArrayInputStream in) {
        measurePalettedContainer(in);
    }

    private static int measurePalettedContainer(ByteArrayInputStream in) {
        in.mark(8192);
        int before = in.available();
        int bitsPerEntry = in.read();
        if (bitsPerEntry == 0) {
            readVarInt(in);
        } else if (bitsPerEntry < 16) {
            int paletteSize = readVarInt(in);
            for (int i = 0; i < paletteSize; i++) {
                readVarInt(in);
            }
            int dataLength = readVarInt(in);
            skipFully(in, dataLength * 8L);
        } else {
            int dataLength = readVarInt(in);
            skipFully(in, dataLength * 8L);
        }
        int after = in.available();
        try {
            in.reset();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return before - after;
    }

    private static void skipFully(ByteArrayInputStream in, long n) {
        long skipped = 0;
        while (skipped < n) {
            long s = in.skip(n - skipped);
            if (s <= 0) break;
            skipped += s;
        }
    }

    private static void writeVarInt(ByteArrayOutputStream out, int value) {
        while ((value & 0xFFFFFF80) != 0) {
            out.write((value & 0x7F) | 0x80);
            value >>>= 7;
        }
        out.write(value);
    }

    private static int readVarInt(ByteArrayInputStream in) {
        int result = 0;
        int shift = 0;
        int b;
        do {
            b = in.read();
            if (b < 0) return result;
            result |= (b & 0x7F) << shift;
            shift += 7;
        } while ((b & 0x80) != 0);
        return result;
    }
}
