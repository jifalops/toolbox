package com.jifalops.toolbox.android.deviceinfoelement;

import android.os.Build;
import android.os.StatFs;
import android.text.TextUtils;
import android.util.Log;

import com.jifalops.toolbox.android.util.ShellHelper;

import java.util.ArrayList;
import java.util.List;


public class Storage {
	private static final String TAG = Storage.class.getSimpleName();

	private static final String
            PROC_MOUNTS = "mounts",         PROC_PARTITIONS = "partitions",
            MOUNT_SDCARDS = ".*sd[^/]*",    MOUNT_SYSTEM = "/system",
            MOUNT_DATA = "/data",           MOUNT_CACHE = "/cache",
            MOUNT_ROOT = "/";
	
	private final List<Mount> mounts = new ArrayList<>();
	private final List<Partition> partitions = new ArrayList<>();
	
	private static Storage instance;
	public static Storage getInstance() {
		if (instance == null) instance = new Storage();
		return instance;
	}
	
	//TODO android.os.Environment
	private Storage() {
		if (!updateMounts()) 
			Log.e(TAG, "Error updating mounts.");
		if (!updatePartitions()) 
			Log.e(TAG, "Error updating partitions.");
	}

    /** Get the current mounts from /proc */
    public boolean updateMounts() {
        List<String> mounts = ShellHelper.getProc(PROC_MOUNTS);
        if (mounts == null || mounts.isEmpty()) return false;
        this.mounts.clear();
        for (String s : mounts) {
            if (s == null || s.length() == 0) continue;
            this.mounts.add(new Mount(s));
        }
        return !this.mounts.isEmpty();
    }

    private boolean updatePartitions() {
        List<String> partitions = ShellHelper.getProc(PROC_PARTITIONS);
        if (partitions == null || partitions.isEmpty()) return false;
        this.partitions.clear();
        boolean first = true;
        for (String s : partitions) {
            // Skip the column headers
            if (first) first = false;
			else if (!TextUtils.isEmpty(s)) this.partitions.add(new Partition(s));
        }
        return !this.partitions.isEmpty();
    }

    public List<Mount> getMounts() {
        return mounts;
    }

    public Mount getMountByPath(String mountPoint) {
        if (mountPoint == null || mountPoint.length() == 0) return null;
        for (Mount m : mounts) {
            if (mountPoint.equals(m.getMountPoint())) {
                return m;
            }
        }
        return null;
    }

    public List<Mount> findMountsByPath(String regex) {
        if (regex == null || regex.length() == 0) return null;
        List<Mount> matches = new ArrayList<Mount>();
        for (Mount m : mounts) {
            if (m.getMountPoint().matches(regex)) {
                matches.add(m);
            }
        }
        return matches;
    }

    public List<Mount> getSdcardMounts() {
        return findMountsByPath(MOUNT_SDCARDS);
    }

    public Mount getSystemMount() {
        return getMountByPath(MOUNT_SYSTEM);
    }

    public Mount getDataMount() {
        return getMountByPath(MOUNT_DATA);
    }

    public Mount getCacheMount() {
        return getMountByPath(MOUNT_CACHE);
    }

    public Mount getRootMount() {
        return getMountByPath(MOUNT_ROOT);
    }

    public List<Partition> getPartitions() {
        return partitions;
    }

    public List<Partition> getAliasedPartitions() {
        List<Partition> list = new ArrayList<Partition>();
        for (Partition p : partitions) {
            if (p.getAlias() != null) {
                list.add(p);
            }
        }
        return list;
    }

    public Partition getPartitionByAlias(String alias) {
        if (alias == null || alias.length() == 0) return null;
        for (Partition p : partitions) {
            if (alias.equals(p.getAlias())) {
                return p;
            }
        }
        return null;
    }

    public Partition getBootPartition() {
        return getPartitionByAlias("boot");
    }

    public Partition getRecoveryPartition() {
        return getPartitionByAlias("recovery");
    }
	
	public static class Partition {
		// TODO look up block size
		/** It *seems* that the partition block sizes are 
		 * (almost) always 1024. Frequently enough to count
		 * on it for now =[
		 */
		public static final int BLOCK_SIZE = 1024;

        private static final String PROC_DEVICES = "devices";

		private int mMajor;
		private int mMinor;
		private int mBlocks;
		private String mName;
		private String mAlias;
		
		private long mTotalSize;
		
		private String mDevice;
		
		public Partition(String desc) {
			if (desc == null || desc.length() == 0) {
				Log.e(TAG, "Error creating Partition instance.");
				return;
			}
			
			String[] parts = desc.split("\\s+");
			
			if (parts.length < 4) {
				Log.e(TAG, "Error creating Partition instance. Unrecognized format.");
			}
			
			try {
				mMajor = Integer.valueOf(parts[0]);
				mMinor = Integer.valueOf(parts[1]);
				mBlocks = Integer.valueOf(parts[2]);
				mName = parts[3];
				mAlias = parts[4]; // optional
			}
			catch (IndexOutOfBoundsException ignored) {}
			catch (NumberFormatException ignored) {}
			catch (NullPointerException ignored) {}
			
			mTotalSize = ((long) mBlocks) * ((long) BLOCK_SIZE);
			
			mDevice = getDevice(mMajor);
		}
		
		private String getDevice(int major) {
			List<String> devices = ShellHelper.getProc(PROC_DEVICES);
	        if (devices == null || devices.isEmpty()) return null;
	        // return *last* match (to match in "Block devices:")
	        String device = null;
	        String[] parts;
	        for (String s : devices) {
	        	if (s == null || s.length() == 0) continue;
	        	parts = s.split("\\s+");
	        	try {
	        		if (Integer.valueOf(parts[0]) == major) {
	        			device = parts[1];
	        		}
	        	} catch (NumberFormatException ignored) {}
	        }
	        return device;
		}
		
		public String getDevice() {
			return mDevice;
		}
		
		public int getDeviceMajor() {
			return mMajor;
		}
		
		public int getDeviceMinor() {
			return mMinor;
		}
		
		public int getNumBlocks() {
			return mBlocks;
		}
		
		public int getBlockSize() {
			return BLOCK_SIZE;
		}
		
		public long getTotalSize() {
			return mTotalSize;
		}
		
		public String getName() {
			return mName;
		}
		
		public String getAlias() {
			return mAlias;
		}
	}
	
	public static class Mount {
		private static final int NO_STATFS = -1;
		
		private String mDevice;
		private String mMountPoint;
		private String mFileSystem;
		private String mAttributesString;
		private String[] mAttributes;
		
		private StatFs mStatFs;
		private long mBlockSize;
		private long mBlockCount;
		private long mTotalSize;
		
		public Mount(String desc) {
			if (desc == null || desc.length() == 0) {
				Log.e(TAG, "Error creating Mount instance.");
				return;
			}
			
			String[] parts = desc.split("\\s+");
			
			if (parts.length != 6) {
				Log.e(TAG, "Error creating Mount instance. Unrecognized format.");
			}
			
			try {
				mDevice = parts[0];
				mMountPoint = parts[1];
				mFileSystem = parts[2];
				mAttributesString = parts[3];
				mAttributes = parts[3].split(",");
			}
			catch (IndexOutOfBoundsException ignored) {}
			catch (NullPointerException ignored) {}
			
			// Initialize to -1 to represent not being able to
			// use a StatFs on this mount.
			mStatFs = null;
			mBlockSize = NO_STATFS; 
			mBlockCount = NO_STATFS;
			mTotalSize = NO_STATFS;
			try { 
				mStatFs = new StatFs(mMountPoint);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
					mBlockSize = mStatFs.getBlockSizeLong();
					mBlockCount = mStatFs.getBlockCountLong();
				} else {
					mBlockSize = mStatFs.getBlockSize();
					mBlockCount = mStatFs.getBlockCount();
				}
				mTotalSize = mBlockSize * mBlockCount;
			} catch (Exception ignored) {}
		}
		
		public boolean hasAttribute(String attr) {
			if (attr == null || attr.length() == 0)
			for (String s : mAttributes) {
				if (attr.equals(s)) return true;
			}
			return false;
		}
		
		public StatFs getStatFs() {
			return mStatFs; 
		}
		
		public long getBlockSize() {
			return mBlockSize;
		}
		
		public long getBlockCount() {
			return mBlockCount;
		}
		
	    public long getTotalSize() { 
	    	return mTotalSize;
	    } 
	    
	    public long getFreeSpace() {
	    	if (mStatFs == null) return NO_STATFS;
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
				return mBlockSize * mStatFs.getFreeBlocksLong();
			} else {
				return mBlockSize * mStatFs.getFreeBlocks();
			}
	    }
	    
	    public long getAvailableSpace() {
	    	if (mStatFs == null) return NO_STATFS;
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
				return mBlockSize * mStatFs.getAvailableBlocksLong();
			} else {
				return mBlockSize * mStatFs.getAvailableBlocks();
			}
	    }

		public boolean isReadOnly() {
			return hasAttribute("ro");
		}
		
		public String getDevice() {
			return mDevice;
		}
		
		public String getMountPoint() {
			return mMountPoint;
		}
		
		public String getFileSystem() {
			return mFileSystem;
		}
		
		public String[] getAttributes() {
			return mAttributes;
		}
		
		public String getAttributesString() {
			return mAttributesString;
		}
	}
}
