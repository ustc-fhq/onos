/*
 * Copyright 2014-present Open Networking Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onosproject.net.flow.instructions;

import org.onosproject.floodlightpof.protocol.OFMatch20;
import org.onosproject.floodlightpof.protocol.action.OFAction;
import org.onosproject.floodlightpof.protocol.instruction.*;

import java.util.List;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Factory class for creating various traffic treatment instructions.
 */
public final class DefaultPofInstructions {

    private static final String SEPARATOR = ":";

    // Ban construction
    private DefaultPofInstructions() {}

    /**
     *
     */
    public static PofInstruction applyActions(List<OFAction> actions) {
        checkNotNull(actions, "actions cannot be null");
        return new PofInstructionApplyActions(actions);
    }


    /**
     *
     */
    public static PofInstruction calculateField(OFInstructionCalculateField.OFCalcType type,
                                                byte srcValueType, OFMatch20 destField,
                                                int srcValue, OFMatch20 srcField) {
        checkNotNull(type, "type cannot be null");
        checkNotNull(srcValueType, "srcValueType cannot be null");
        checkNotNull(destField, "destField cannot be null");
        checkNotNull(srcValue, "srcValue cannot be null");
        checkNotNull(srcField, "srcField cannot be null");

        return new PofInstructionCalcField(type, srcValueType, destField, srcValue, srcField);
    }

    public static PofInstruction calculateField(OFInstructionCalculateField calcField) {
        checkNotNull(calcField, "calcField cannot be null");

        return new PofInstructionCalcField(calcField);
    }

    /**
     *
     */
    public static PofInstruction gotoDirectTable(byte nextTableId, byte indexType,
                                                 short packetOffset, int indexValue,
                                                 OFMatch20 indexFiled) {
        checkNotNull(nextTableId, "nextTableId cannot be null");
        checkNotNull(indexType, "indexType cannot be null");
        checkNotNull(packetOffset, "packetOffset cannot be null");
        checkNotNull(indexValue, "indexValue cannot be null");
        checkNotNull(indexFiled, "indexFiled cannot be null");

        return new PofInstructionGotoDirectTable(nextTableId, indexType, packetOffset, indexValue, indexFiled);
    }
    public static PofInstruction gotoDirectTable(OFInstructionGotoDirectTable gotoDirectTable) {
        checkNotNull(gotoDirectTable, "gotoDirectTable cannot be null");

        return new PofInstructionGotoDirectTable(gotoDirectTable);
    }

    /**
     *
     */
    public static PofInstruction gotoTable(byte nextTableId, byte matchFieldNum,
                                           short packetOffset, List<OFMatch20> matchList) {
        checkNotNull(nextTableId, "nextTableId cannot be null");
        checkNotNull(matchFieldNum, "matchFieldNum cannot be null");
        checkNotNull(packetOffset, "packetOffset cannot be null");
        checkNotNull(matchList, "matchList cannot be null");

        return new PofInstructionGotoTable(nextTableId, matchFieldNum, packetOffset, matchList);
    }

    public static PofInstruction gotoTable(OFInstructionGotoTable gotoTable) {
        checkNotNull(gotoTable, "gotoTable cannot be null");

        return new PofInstructionGotoTable(gotoTable);
    }
    public static PofInstruction meter(int meterId) {
        checkNotNull(meterId, "meterId cannot be null");
        return new PofInstructionMeter(meterId);
    }
    public static PofInstruction meter(OFInstructionMeter ofInstructionMeter){
        checkNotNull(ofInstructionMeter,"pofInstructionMeter cannot be null");
        return new PofInstructionMeter(ofInstructionMeter);
    }


    /**
     *
     */
    public static PofInstruction writeMetadata(short metadataOffset, short writeLength, byte[] value) {
        checkNotNull(metadataOffset, "metadataOffset cannot be null");
        checkNotNull(writeLength, "writeLength cannot be null");
        checkNotNull(value, "value cannot be null");

        return new PofInstructionWriteMetadata(metadataOffset, writeLength, value);
    }

    public static PofInstruction writeMetadata(OFInstructionWriteMetadata writeMetadata) {
        checkNotNull(writeMetadata, "writeMetadata cannot be null");

        return new PofInstructionWriteMetadata(writeMetadata);
    }

    /**
     *
     */
    public static PofInstruction writeMetadataFromPacket(short metadataOffset, short packetOffset, short Length) {
        checkNotNull(metadataOffset, "metadataOffset cannot be null");
        checkNotNull(Length, "Length cannot be null");
        checkNotNull(packetOffset, "packetOffset cannot be null");

        return new PofInstructionWriteMetadataFromPacket(metadataOffset, packetOffset, Length);
    }
    public static PofInstruction writeMetadataFromPacket(short metadataOffset, short packetOffset) {
        checkNotNull(metadataOffset, "metadataOffset cannot be null");
        checkNotNull(packetOffset, "packetOffset cannot be null");
        short length = 8;
        return new PofInstructionWriteMetadataFromPacket(metadataOffset, packetOffset, length);
    }
    public static PofInstruction writeMetadataFromPacket(OFInstructionWriteMetadataFromPacket writeMetadataFromPacket) {
        checkNotNull(writeMetadataFromPacket, "writeMetadataFromPacket cannot be null");

        return new PofInstructionWriteMetadataFromPacket(writeMetadataFromPacket);
    }

    /**
    *
    */
    public static PofInstruction GetMetadata(short metadaOffset, short Length, byte[] value) {
        checkNotNull(metadaOffset, "metadataOffset cannot be null");
        checkNotNull(Length, "Length cannot be null");
        checkNotNull(value, "value cannot be null");

        return new PofInstructionGetMetadata(metadaOffset, Length, value);
    }

    public static PofInstruction GetMetadata(OFInstructionGetMetadate getMetadate){
        checkNotNull(getMetadate, "getMetadata cannot be null");

        return new PofInstructionGetMetadata(getMetadate);
    }
    /**
     *
     */
    public static PofInstruction Branch(int skipInstructionNum, byte operand2Type, OFMatch20 operand1Field, int operand2Value, OFMatch20 operand2Field) {
        checkNotNull(operand2Type, "operand2type cannot be null");

        return new PofInstructionBranch(skipInstructionNum,operand2Type, operand1Field,operand2Value, operand2Field);
    }

    public static PofInstruction Branch(OFInstructionBranch branch){
        checkNotNull(branch, "branch cannot be null");

        return new PofInstructionBranch(branch);
    }
    /**
     *
     */
    public static PofInstruction RelativeJump(byte direction, int offset) {
        checkNotNull(direction, "direction cannot be null");
        checkNotNull(offset, "ofset cannot be null");

        return new PofInstructionRelativeJump(direction, offset);
    }

    public static PofInstruction RelativeJump(OFInstructionRelativeJump relativeJump){
        checkNotNull(relativeJump, "RelativeJump cannot be null");

        return new PofInstructionRelativeJump(relativeJump);
    }
    /**
     *
     */
    public static PofInstruction ConditionalRelativeJump(byte field2Type, byte off1Dir, byte off2Dir, byte off3Dir, OFMatch20 cmpField1, int cmpField2Value,
                                                         OFMatch20 cmpField2, int off1, int off2, int off3) {
        checkNotNull(field2Type, "field2Type cannot be null");


        return new PofInstructionConditionalRelativeJump(field2Type, off1Dir, off2Dir,off3Dir, cmpField1, cmpField2Value,cmpField2,off1,off2,off3);
    }

    public static PofInstruction ConditionalRelativeJump(OFInstructionConditionalRelativeJump conditionalRelativeJump){
        checkNotNull(conditionalRelativeJump, "ConditionalRelativeJump cannot be null");

        return new PofInstructionConditionalRelativeJump(conditionalRelativeJump);
    }
    /**
     *
     */
    public static PofInstruction Compare(byte operand2Type, OFMatch20 cmpResField, OFMatch20 operand1Field, int operand2Value,
                                         OFMatch20 operand2Field) {
        checkNotNull(operand2Type, "operand2Type cannot be null");


        return new PofInstructionCompare(operand2Type, cmpResField, operand1Field, operand2Value, operand2Field);
    }

    public static PofInstruction Compare(OFInstructionCompare compare){
        checkNotNull(compare, "Compare cannot be null");

        return new PofInstructionCompare(compare);
    }
    /**
     *  apply actions Instruction.
     */
    public static final class PofInstructionApplyActions implements PofInstruction {

        private final OFInstructionApplyActions insApplyActions;

        private PofInstructionApplyActions(List<OFAction> actions) {
            this.insApplyActions = new OFInstructionApplyActions();
            this.insApplyActions.setActionNum((byte) actions.size());
            this.insApplyActions.setActionList(actions);
        }

        @Override
        public PofInstructionType pofInstructionType() {
            return PofInstructionType.POF_ACTION;
        }

        @Override
        public OFInstruction instruction() {
            return this.insApplyActions;
        }

        @Override
        public Instruction.Type type() {
            return Instruction.Type.POFINSTRUCTION;
        }

        @Override
        public String toString() {
            return type().toString() + SEPARATOR + insApplyActions.toString();
        }

        @Override
        public int hashCode() {
            return Objects.hash(type().ordinal(), insApplyActions);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj instanceof OFInstructionApplyActions) {
                OFInstructionApplyActions that = (OFInstructionApplyActions) obj;
                return Objects.equals(insApplyActions.getActionList(), that.getActionList());

            }
            return false;
        }
    }

    /**
     *  calc field Instruction.
     */
    public static final class PofInstructionCalcField implements PofInstruction {
        private final OFInstructionCalculateField insCalculateField;

        private PofInstructionCalcField(OFInstructionCalculateField.OFCalcType type,
                                        byte srcValueType, OFMatch20 destField,
                                        int srcValue, OFMatch20 srcField) {
            insCalculateField = new OFInstructionCalculateField();
            insCalculateField.setCalcType(type);
            insCalculateField.setSrcValueType(srcValueType);
            insCalculateField.setDesField(destField);
            insCalculateField.setSrcValue(srcValue);
            insCalculateField.setSrcField(srcField);
        }

        private PofInstructionCalcField(OFInstructionCalculateField calcField) {
            this.insCalculateField = calcField;
        }

        @Override
        public PofInstructionType pofInstructionType() {
            return PofInstructionType.CALCULATE_FIELD;
        }

        @Override
        public OFInstruction instruction() {
            return this.insCalculateField;
        }

        @Override
        public Instruction.Type type() {
            return Instruction.Type.POFINSTRUCTION;
        }

        @Override
        public String toString() {
            return type().toString() + SEPARATOR + insCalculateField.toString();
        }

        @Override
        public int hashCode() {
            return Objects.hash(type().ordinal(), insCalculateField);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj instanceof OFInstructionCalculateField) {
                OFInstructionCalculateField that = (OFInstructionCalculateField) obj;
                return Objects.equals(insCalculateField.getSrcField(), that.getSrcField()) &&
                        Objects.equals(insCalculateField.getDesField(), that.getDesField());

            }
            return false;
        }
    }

    /**
     *  PofInstructionGotoDirectTable Instruction.
     */
    public static final class PofInstructionGotoDirectTable implements PofInstruction {
        private final OFInstructionGotoDirectTable insGotoDirectTable;

        private PofInstructionGotoDirectTable(byte nextTableId, byte indexType,
                                              short packetOffset, int indexValue,
                                              OFMatch20 indexFiled) {
            insGotoDirectTable = new OFInstructionGotoDirectTable();
            insGotoDirectTable.setNextTableId(nextTableId);
            insGotoDirectTable.setIndexType(indexType);
            insGotoDirectTable.setPacketOffset(packetOffset);
            insGotoDirectTable.setIndexValue(indexValue);
            insGotoDirectTable.setIndexField(indexFiled);
        }

        private PofInstructionGotoDirectTable(OFInstructionGotoDirectTable gotoDirectTable) {
            this.insGotoDirectTable = gotoDirectTable;
        }

        @Override
        public PofInstructionType pofInstructionType() {
            return PofInstructionType.GOTO_DIRECT_TABLE;
        }

        @Override
        public OFInstruction instruction() {
            return this.insGotoDirectTable;
        }

        @Override
        public Instruction.Type type() {
            return Instruction.Type.POFINSTRUCTION;
        }

        @Override
        public String toString() {
            return type().toString() + SEPARATOR + insGotoDirectTable.toString();
        }

        @Override
        public int hashCode() {
            return Objects.hash(type().ordinal(), insGotoDirectTable);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj instanceof OFInstructionGotoDirectTable) {
                OFInstructionGotoDirectTable that = (OFInstructionGotoDirectTable) obj;
                return Objects.equals(insGotoDirectTable.getNextTableId(), that.getNextTableId()) &&
                        Objects.equals(insGotoDirectTable.getIndexValue(), that.getIndexValue());

            }
            return false;
        }
    }


    /**
     *  PofInstructionGotoDTable Instruction.
     */
    public static final class PofInstructionGotoTable implements PofInstruction {
        private final OFInstructionGotoTable insGotoTable;

        private PofInstructionGotoTable(byte nextTableId, byte matchFieldNum,
                                        short packetOffset, List<OFMatch20> matchList) {
            insGotoTable = new OFInstructionGotoTable();
            insGotoTable.setNextTableId(nextTableId);
            insGotoTable.setMatchFieldNum(matchFieldNum);
            insGotoTable.setPacketOffset(packetOffset);
            insGotoTable.setMatchList(matchList);
        }

        private PofInstructionGotoTable(OFInstructionGotoTable gotoTable) {
            this.insGotoTable = gotoTable;
        }

        @Override
        public PofInstructionType pofInstructionType() {
            return PofInstructionType.GOTO_TABLE;
        }

        @Override
        public OFInstruction instruction() {
            return this.insGotoTable;
        }

        @Override
        public Instruction.Type type() {
            return Instruction.Type.POFINSTRUCTION;
        }

        @Override
        public String toString() {
            return type().toString() + SEPARATOR + insGotoTable.toString();
        }

        @Override
        public int hashCode() {
            return Objects.hash(type().ordinal(), insGotoTable);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj instanceof OFInstructionGotoTable) {
                OFInstructionGotoTable that = (OFInstructionGotoTable) obj;
                return Objects.equals(insGotoTable.getNextTableId(), that.getNextTableId()) &&
                        Objects.equals(insGotoTable.getPacketOffset(), that.getPacketOffset()) &&
                        Objects.equals(insGotoTable.getMatchList(), that.getMatchList());

            }
            return false;
        }
    }
    /**
     * PofInstructionMeter Instruction.
     */
    public static final class PofInstructionMeter implements PofInstruction {
        private final OFInstructionMeter ofInstructionMeter;

        private PofInstructionMeter(int meterId) {
            ofInstructionMeter = new OFInstructionMeter();

            ofInstructionMeter.setMeterId(meterId);
        }

        private PofInstructionMeter(OFInstructionMeter ofInstructionMeter) {
            this.ofInstructionMeter = ofInstructionMeter;
        }

        @Override
        public PofInstructionType pofInstructionType() {
            return PofInstructionType.METER;
        }

        @Override
        public OFInstruction instruction() {
            return this.ofInstructionMeter;
        }

        @Override
        public Instruction.Type type() {
            return Instruction.Type.POFINSTRUCTION;
        }

        @Override
        public String toString() {
            return type().toString() + SEPARATOR + ofInstructionMeter.toString();
        }

        @Override
        public int hashCode() {
            return Objects.hash(type().ordinal(), ofInstructionMeter);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj instanceof OFInstructionMeter) {
                OFInstructionMeter that = (OFInstructionMeter) obj;
                return Objects.equals(ofInstructionMeter.getMeterId(), that.getMeterId());

            }
            return false;
        }

    }

    /**
     *  PofInstructionWriteMetadata Instruction.
     */
    public static final class PofInstructionWriteMetadata implements PofInstruction {
        private final OFInstructionWriteMetadata insWriteMetadata;

        private PofInstructionWriteMetadata(short metadataOffset, short writeLength, byte[] value) {
            insWriteMetadata = new OFInstructionWriteMetadata();
            insWriteMetadata.setMetadataOffset(metadataOffset);
            insWriteMetadata.setWriteLength(writeLength);
            insWriteMetadata.setValue(value);

        }

        private PofInstructionWriteMetadata(OFInstructionWriteMetadata writeMetadata) {
            this.insWriteMetadata = writeMetadata;
        }

        @Override
        public PofInstructionType pofInstructionType() {
            return PofInstructionType.WRITE_METADATA;
        }

        @Override
        public OFInstruction instruction() {
            return this.insWriteMetadata;
        }

        @Override
        public Instruction.Type type() {
            return Instruction.Type.POFINSTRUCTION;
        }

        @Override
        public String toString() {
            return type().toString() + SEPARATOR + insWriteMetadata.toString();
        }

        @Override
        public int hashCode() {
            return Objects.hash(type().ordinal(), insWriteMetadata);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj instanceof OFInstructionWriteMetadata) {
                OFInstructionWriteMetadata that = (OFInstructionWriteMetadata) obj;
                return Objects.equals(insWriteMetadata.getMetadataOffset(), that.getMetadataOffset()) &&
                        Objects.equals(insWriteMetadata.getWriteLength(), that.getWriteLength()) &&
                        Objects.equals(insWriteMetadata.getValue(), that.getValue());

            }
            return false;
        }
    }

    /**
     *  PofInstructionWriteMetadataFromPacket Instruction.
     */
    public static final class PofInstructionWriteMetadataFromPacket implements PofInstruction {
        private final OFInstructionWriteMetadataFromPacket insWriteMetadataFromPacket;

        private PofInstructionWriteMetadataFromPacket(short metadataOffset, short packetOffset, short Length) {
            insWriteMetadataFromPacket = new OFInstructionWriteMetadataFromPacket();
            insWriteMetadataFromPacket.setMetadataOffset(metadataOffset);
            insWriteMetadataFromPacket.setPacketOffset(packetOffset);
            // insWriteMetadataFromPacket.setLength(Length);

        }

        private PofInstructionWriteMetadataFromPacket(OFInstructionWriteMetadataFromPacket writeMetadataFromPacket) {
            this.insWriteMetadataFromPacket = writeMetadataFromPacket;
        }

        @Override
        public PofInstructionType pofInstructionType() {
            return PofInstructionType.WRITE_METADATA_FROM_PACKET;
        }

        @Override
        public OFInstruction instruction() {
            return this.insWriteMetadataFromPacket;
        }

        @Override
        public Instruction.Type type() {
            return Instruction.Type.POFINSTRUCTION;
        }

        @Override
        public String toString() {
            return type().toString() + SEPARATOR + insWriteMetadataFromPacket.toString();
        }

        @Override
        public int hashCode() {
            return Objects.hash(type().ordinal(), insWriteMetadataFromPacket);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj instanceof OFInstructionWriteMetadataFromPacket) {
                OFInstructionWriteMetadataFromPacket that = (OFInstructionWriteMetadataFromPacket) obj;
                return Objects.equals(insWriteMetadataFromPacket.getMetadataOffset(), that.getMetadataOffset()) &&
                        Objects.equals(insWriteMetadataFromPacket.getLength(), that.getLength()) &&
                        Objects.equals(insWriteMetadataFromPacket.getPacketOffset(), that.getPacketOffset());

            }
            return false;
        }
    }

    /**
     *  PofInstructionGetMetadata Instruction.
     */
    public static final class PofInstructionGetMetadata implements PofInstruction {
        private final OFInstructionGetMetadate insGetMetadata;

        private PofInstructionGetMetadata(short metadataOffset, short Length, byte[] value) {
            insGetMetadata = new OFInstructionGetMetadate();
            insGetMetadata.setMetadataOffset(metadataOffset);
            insGetMetadata.setLength(Length);
            insGetMetadata.setValue(value);

        }

        private PofInstructionGetMetadata(OFInstructionGetMetadate GetMetadata) {
            this.insGetMetadata = GetMetadata;
        }

        @Override
        public PofInstructionType pofInstructionType() {
            return PofInstructionType.GET_METADATA;
        }

        @Override
        public OFInstruction instruction() {
            return this.insGetMetadata;
        }

        @Override
        public Instruction.Type type() {
            return Instruction.Type.POFINSTRUCTION;
        }

        @Override
        public String toString() {
            return type().toString() + SEPARATOR + insGetMetadata.toString();
        }

        @Override
        public int hashCode() {
            return Objects.hash(type().ordinal(), insGetMetadata);
        }

        @Override
        public boolean equals(Object obj) {
            return insGetMetadata.equals(obj);
        }
    }
    /**
     *  PofInstructionBranch Instruction.
     */
    public static final class PofInstructionBranch implements PofInstruction {
        private final OFInstructionBranch insBranch;

        private PofInstructionBranch(int skipInstructionNum, byte operand2Type, OFMatch20 operand1Field, int operand2Value, OFMatch20 operand2Field) {
            insBranch = new OFInstructionBranch();
            insBranch.setSkipInstructionNum(skipInstructionNum);
            insBranch.setOperand2Value(operand2Value);
            insBranch.setOperand2Type(operand2Type);
            insBranch.setOperand1Field(operand1Field);
            insBranch.setOperand2Field(operand2Field);

        }

        private PofInstructionBranch(OFInstructionBranch Branch) {
            this.insBranch = Branch;
        }

        @Override
        public PofInstructionType pofInstructionType() {
            return PofInstructionType.GET_METADATA;
        }

        @Override
        public OFInstruction instruction() {
            return this.insBranch;
        }

        @Override
        public Instruction.Type type() {
            return Instruction.Type.POFINSTRUCTION;
        }

        @Override
        public String toString() {
            return type().toString() + SEPARATOR + insBranch.toString();
        }

        @Override
        public int hashCode() {
            return Objects.hash(type().ordinal(), insBranch);
        }

        @Override
        public boolean equals(Object obj) {
            return insBranch.equals(obj);
        }
    }

    /**
     *  PofInstructionRelativejumpInstruction.
     */
    public static final class PofInstructionRelativeJump implements PofInstruction {
        private final OFInstructionRelativeJump insRelativeJump;

        private PofInstructionRelativeJump(byte direction, int offset) {
            insRelativeJump = new OFInstructionRelativeJump();
            insRelativeJump.setDirection(direction);
            insRelativeJump.setOffset(offset);
        }

        private PofInstructionRelativeJump(OFInstructionRelativeJump relativeJump) {
            this.insRelativeJump = relativeJump;
        }

        @Override
        public PofInstructionType pofInstructionType() {
            return PofInstructionType.RELATIVE_JUMP;
        }

        @Override
        public OFInstruction instruction() {
            return this.insRelativeJump;
        }

        @Override
        public Instruction.Type type() {
            return Instruction.Type.POFINSTRUCTION;
        }

        @Override
        public String toString() {
            return type().toString() + SEPARATOR + insRelativeJump.toString();
        }

        @Override
        public int hashCode() {
            return Objects.hash(type().ordinal(), insRelativeJump);
        }

        @Override
        public boolean equals(Object obj) {
            return insRelativeJump.equals(obj);
        }
    }
    /**
     *  PofInstructionConditionalRelativejumpInstruction.
     */
    public static final class PofInstructionConditionalRelativeJump implements PofInstruction {
        private final OFInstructionConditionalRelativeJump insConditionalRelativeJump;

        private PofInstructionConditionalRelativeJump( byte field2Type, byte off1Dir, byte off2Dir, byte off3Dir, OFMatch20 cmpField1, int cmpField2Value,
                                                       OFMatch20 cmpField2, int off1, int off2, int off3) {
            insConditionalRelativeJump = new OFInstructionConditionalRelativeJump();
            insConditionalRelativeJump.setField2Type(field2Type);
            insConditionalRelativeJump.setOff1Dir(off1Dir);
            insConditionalRelativeJump.setOff1(off1);
            insConditionalRelativeJump.setOff2Dir(off2Dir);
            insConditionalRelativeJump.setOff2(off2);
            insConditionalRelativeJump.setOff3Dir(off3Dir);
            insConditionalRelativeJump.setOff3(off3);
            insConditionalRelativeJump.setCmpField1(cmpField1);
            insConditionalRelativeJump.setCmpField2Value(cmpField2Value);
            insConditionalRelativeJump.setCmpField2(cmpField2);
        }

        private PofInstructionConditionalRelativeJump(OFInstructionConditionalRelativeJump conditionalRelativeJump) {
            this.insConditionalRelativeJump = conditionalRelativeJump;
        }

        @Override
        public PofInstructionType pofInstructionType() {
            return PofInstructionType.CONDITIONAL_RELATIVE_JUMP;
        }

        @Override
        public OFInstruction instruction() {
            return this.insConditionalRelativeJump;
        }

        @Override
        public Instruction.Type type() {
            return Instruction.Type.POFINSTRUCTION;
        }

        @Override
        public String toString() {
            return type().toString() + SEPARATOR + insConditionalRelativeJump.toString();
        }

        @Override
        public int hashCode() {
            return Objects.hash(type().ordinal(), insConditionalRelativeJump);
        }

        @Override
        public boolean equals(Object obj) {
            return insConditionalRelativeJump.equals(obj);
        }
    }
    /**
     *  PofInstructionConditionalRelativejumpInstruction.
     */
    public static final class PofInstructionCompare implements PofInstruction {
        private final OFInstructionCompare insCompare;

        private PofInstructionCompare( byte operand2Type, OFMatch20 cmpResField, OFMatch20 operand1Field, int operand2Value,
                                                       OFMatch20 operand2Field) {
            insCompare = new OFInstructionCompare();
            insCompare.setOperand2Type(operand2Type);
            insCompare.setCmpResField(cmpResField);
            insCompare.setOperand1Field(operand1Field);
            insCompare.setOperand2Value(operand2Value);
            insCompare.setOperand2Field(operand2Field);
        }

        private PofInstructionCompare(OFInstructionCompare compare) {
            this.insCompare = compare;
        }

        @Override
        public PofInstructionType pofInstructionType() {
            return PofInstructionType.COMPARE;
        }

        @Override
        public OFInstruction instruction() {
            return this.insCompare;
        }

        @Override
        public Instruction.Type type() {
            return Instruction.Type.POFINSTRUCTION;
        }

        @Override
        public String toString() {
            return type().toString() + SEPARATOR + insCompare.toString();
        }

        @Override
        public int hashCode() {
            return Objects.hash(type().ordinal(), insCompare);
        }

        @Override
        public boolean equals(Object obj) {
            return insCompare.equals(obj);
        }
    }
}

