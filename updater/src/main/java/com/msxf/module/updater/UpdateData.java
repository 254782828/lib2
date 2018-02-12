package com.msxf.module.updater;

/**
 * @author Vincent Cheung (coolingfall@gmail.com)
 */
public final class UpdateData {
  public final Version data;
  public final String code;
  public final String message;

  private UpdateData(Builder builder) {
    data = builder.data;
    code = builder.code;
    message = builder.message;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private Version data;
    private String code;
    private String message;

    private Builder() {
    }

    public Builder data(Version data) {
      this.data = data;
      return this;
    }

    public Builder code(String code) {
      this.code = code;
      return this;
    }

    public Builder message(String message) {
      this.message = message;
      return this;
    }

    public UpdateData build() {
      return new UpdateData(this);
    }
  }
}
