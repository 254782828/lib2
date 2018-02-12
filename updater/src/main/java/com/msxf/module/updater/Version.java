package com.msxf.module.updater;

/**
 * @author Vincent Cheung (coolingfall@gmail.com)
 */
public final class Version {
  public final String name;
  public final String versionName;
  public final long size;
  public final int forceUpdate;
  public final String downloadUrl;
  public final String updateDescription;
  public final String md5;

  private Version(Builder builder) {
    name = builder.name;
    versionName = builder.versionName;
    size = builder.size;
    forceUpdate = builder.forceUpdate;
    downloadUrl = builder.downloadUrl;
    updateDescription = builder.updateDescription;
    md5 = builder.md5;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private String name;
    private String versionName;
    private long size;
    private int forceUpdate;
    private String downloadUrl;
    private String updateDescription;
    private String md5;

    private Builder() {
    }

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public Builder versionName(String versionName) {
      this.versionName = versionName;
      return this;
    }

    public Builder size(long size) {
      this.size = size;
      return this;
    }

    public Builder forceUpdate(int forceUpdate) {
      this.forceUpdate = forceUpdate;
      return this;
    }

    public Builder downloadUrl(String downloadUrl) {
      this.downloadUrl = downloadUrl;
      return this;
    }

    public Builder updateDescription(String updateDescription) {
      this.updateDescription = updateDescription;
      return this;
    }

    public Builder md5(String md5) {
      this.md5 = md5;
      return this;
    }

    public Version build() {
      return new Version(this);
    }
  }
}
