/*
 * jETeL/Clover - Java based ETL application framework.
 * Copyright (c) Opensys TM by Javlin, a.s. (www.opensys.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU   
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 */
package org.jetel.data;

import java.io.UnsupportedEncodingException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.Arrays;

import org.jetel.metadata.DataFieldMetadata;
import org.jetel.util.bytes.ByteBufferUtils;
import org.jetel.util.file.ZipUtils;
import org.jetel.util.string.Compare;

/**
 * Class implementing field which represents gzip-compressed array of bytes. 
 * @author Jan Hadrava (jan.hadrava@javlinconsulting.cz), Javlin Consulting (www.javlinconsulting.cz)
 * @since 12/01/06  
 */
public class CompressedByteDataField extends ByteDataField {
	private static final long serialVersionUID = 1L;
	
	/** lenght of data represented by the field. */ 
	private int dataLen;

	public CompressedByteDataField(DataFieldMetadata _metadata) {
		super(_metadata);
		dataLen = 0;
	}

	public CompressedByteDataField(DataFieldMetadata _metadata, boolean plain) {
		super(_metadata, plain);
		dataLen = 0;
	}

	public CompressedByteDataField(DataFieldMetadata _metadata, byte[] value) {
		super(_metadata, value);
		// dataLen is set when the setValue(byte[]) is called in the constructor of the super class
	}
	
	public DataField duplicate(){
		CompressedByteDataField compressedByteDataField = new CompressedByteDataField(metadata);
		compressedByteDataField.value = (value != null) ? Arrays.copyOf(value, value.length) : null;
		compressedByteDataField.dataLen = dataLen;

		return compressedByteDataField;
	}

	/**
	 * @see org.jetel.data.DataField#copyField(org.jetel.data.DataField)
     * @deprecated use setValue(DataField) instead
	 */
	public void copyFrom(DataField fromField){
	    if (fromField instanceof CompressedByteDataField){
	        if (!fromField.isNull){
	            int length = ((CompressedByteDataField) fromField).value.length;
	            if (this.value == null || this.value.length != length){
	                this.value = new byte[length];
	            }
	            System.arraycopy(((CompressedByteDataField) fromField).value, 0, this.value, 0, length);
	        }
	        setNull(fromField.isNull);
	        dataLen = ((CompressedByteDataField) fromField).dataLen;
	    } else {
	        super.copyFrom(fromField);
        }
	}
	
	public void setValue(byte[] value) {
		dataLen = value == null ? 0 : value.length;
		super.setValue(ZipUtils.compress(value));
	}

	public void setValue(byte value) {
		dataLen = metadata.getSize();
		if (dataLen <= 0) {
			dataLen = INITIAL_BYTE_ARRAY_CAPACITY;
		}
		byte[] buf = new byte[dataLen];

		Arrays.fill(buf, value);
		setValue(buf);
		setNull(false);
	}

    @Override
    public void setValue(DataField fromField) {
        if (fromField instanceof CompressedByteDataField){
            if (!fromField.isNull){
                int length = ((CompressedByteDataField) fromField).value.length;
                if (this.value == null || this.value.length != length){
                    this.value = new byte[length];
                }
                System.arraycopy(((CompressedByteDataField) fromField).value, 0, this.value, 0, length);
            }
            setNull(fromField.isNull);
            dataLen = ((CompressedByteDataField) fromField).dataLen;
        } else {
            super.setValue(fromField);
        }
    }

	public char getType() {
		return DataFieldMetadata.BYTE_FIELD_COMPRESSED;
	}

    public Object getValueDuplicate() {
    	return getValue();
    }

	public byte getByte(int position) {
        if(isNull) {
            return 0;
        }
		return getByteArray()[position];
	}

	public byte[] getByteArray() {
		return ZipUtils.decompress(super.value, dataLen);
	}

	public void fromString(CharSequence seq) {
		fromString(seq, Defaults.DataFormatter.DEFAULT_CHARSET_ENCODER);
	}

	public void fromString(CharSequence seq, String charset) {
		if (seq == null || Compare.equals(seq, metadata.getNullValue())) {
			setNull(true);
			return;
		}

		try {
			byte[] bytes = seq.toString().getBytes(charset);
			setValue(bytes);
			dataLen = bytes.length;
		} catch (UnsupportedEncodingException ex) {
			throw new RuntimeException(ex.toString() + " when calling fromString() on field \""
					+ this.metadata.getName() + "\"", ex);
		}

		setNull(false);
	}

	public void fromByteBuffer(ByteBuffer dataBuffer, CharsetDecoder decoder) {
		dataLen = metadata.getSize();
		if (dataLen <= 0) {
			dataLen = INITIAL_BYTE_ARRAY_CAPACITY;
		}
		byte[] buf = new byte[dataLen];
		dataBuffer.get(buf);
		setValue(buf);
		setNull(false);
	}

	public void toByteBuffer(ByteBuffer dataBuffer, CharsetEncoder encoder) {
        if(!isNull) {
        	try {
        		dataBuffer.put(getByteArray());
        	} catch (BufferOverflowException e) {
    			throw new RuntimeException("The size of data buffer is only " + dataBuffer.limit() + ". Set appropriate parameter in defautProperties file.", e);
        	}
        }
	}

    public void toByteBuffer(ByteBuffer dataBuffer) {
        if(!isNull) {
        	try {
        		dataBuffer.put(getByteArray());
        	} catch (BufferOverflowException e) {
    			throw new RuntimeException("The size of data buffer is only " + dataBuffer.limit() + ". Set appropriate parameter in defautProperties file.", e);
        	}
        }
    }

	public void serialize(ByteBuffer buffer) {
        try {
            if(isNull) {
    			// encode nulls as zero
                ByteBufferUtils.encodeLength(buffer, 0);
            } else {
            	// increment length of non-null values by one
                ByteBufferUtils.encodeLength(buffer, dataLen + 1);
                ByteBufferUtils.encodeLength(buffer, value.length);
               	buffer.put(value);
            }
    	} catch (BufferOverflowException e) {
    		throw new RuntimeException("The size of data buffer is only " + buffer.limit() + ". Set appropriate parameter in defautProperties file.", e);
    	}
	}

	public void deserialize(ByteBuffer buffer) {
		dataLen = ByteBufferUtils.decodeLength(buffer);

		if (dataLen == 0) {
			setNull(true);
			return;
		}

		// encoded length is incremented by one, decrement it back to normal
		dataLen--;

		int bufLen = ByteBufferUtils.decodeLength(buffer);

		if (value == null || bufLen != value.length) {
			value = new byte[bufLen];
		}

		buffer.get(value);
		setNull(false);
	}

	public boolean equals(Object obj) {
	    if (isNull || obj==null) return false;
	    
		if (obj instanceof CompressedByteDataField){
			return this.dataLen == ((CompressedByteDataField) obj).dataLen
									&& Arrays.equals(this.value, ((CompressedByteDataField) obj).value);
		}else if (obj instanceof byte[]){
			return Arrays.equals(getByteArray(), (byte[])obj);
		}else {
		    return false;
		}
	}

	public int compareTo(Object obj) {
		if (isNull) return -1;
		
		byte[] left;
		byte[] right;

		if (obj instanceof CompressedByteDataField){
			left = value;
			right = ((CompressedByteDataField)obj).value;
		}else if (obj instanceof byte[]){
			left = getByteArray();
			right = (byte[])obj;
		}else {
		    throw new ClassCastException("Can't compare CompressedByteDataField and "+obj.getClass().getName());
		}
		 
		int compLength = left.length >= right.length ? left.length : right.length;
		for (int i = 0; i < compLength; i++) {
			if (left[i] > right[i]) {
				return 1;
			} else if (left[i] < right[i]) {
				return -1;
			}
		}
		// arrays seem to be the same (so far), decide according to the length
		if (left.length == right.length) {
			return 0;
		} else if (left.length > right.length) {
			return 1;
		} else {
			return -1;
		}
	}
	
	public int getSizeSerialized() {
        if(isNull) {
            return ByteBufferUtils.lengthEncoded(0);
        } else {
            return ByteBufferUtils.lengthEncoded(dataLen) + ByteBufferUtils.lengthEncoded(value.length) + value.length;
        }
	}
    
	public int getDataLength() {
		return dataLen;
	}
	
}
