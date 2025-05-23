// Copyright 2021-present StarRocks, Inc. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.starrocks.authentication;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.starrocks.common.CaseSensibility;
import com.starrocks.common.PatternMatcher;
import com.starrocks.common.io.Writable;
import com.starrocks.mysql.MysqlPassword;

public class UserAuthenticationInfo implements Writable {
    protected static final String ANY_HOST = "%";
    protected static final String ANY_USER = "%";

    @SerializedName(value = "p")
    private byte[] password = MysqlPassword.EMPTY_PASSWORD;
    @SerializedName(value = "a")
    private String authPlugin = null;
    @SerializedName(value = "t")
    private String authString = null;
    @SerializedName(value = "h")
    private String origHost;
    @SerializedName(value = "u")
    private String origUser;

    @Expose(serialize = false)
    private boolean isAnyUser;
    @Expose(serialize = false)
    private boolean isAnyHost;
    @Expose(serialize = false)
    protected PatternMatcher userPattern;
    @Expose(serialize = false)
    protected PatternMatcher hostPattern;

    public boolean matchUser(String remoteUser) {
        return isAnyUser || userPattern.match(remoteUser);
    }

    public boolean matchHost(String remoteHost) {
        return isAnyHost || hostPattern.match(remoteHost);
    }

    public void analyze() throws AuthenticationException {
        isAnyUser = origUser.equals(ANY_USER);
        isAnyHost = origHost.equals(ANY_HOST);
        userPattern = PatternMatcher.createMysqlPattern(origUser, CaseSensibility.USER.getCaseSensibility());
        hostPattern = PatternMatcher.createMysqlPattern(origHost, CaseSensibility.HOST.getCaseSensibility());
    }

    public byte[] getPassword() {
        return password;
    }

    public String getAuthPlugin() {
        return authPlugin;
    }

    public String getOrigHost() {
        return origHost;
    }

    public String getAuthString() {
        return authString;
    }

    public void setPassword(byte[] password) {
        this.password = password;
    }

    public void setAuthPlugin(String authPlugin) {
        this.authPlugin = authPlugin;
    }

    public void setAuthString(String authString) {
        this.authString = authString;
    }

    public void setOrigUserHost(String origUser, String origHost) throws AuthenticationException {
        this.origUser = origUser;
        this.origHost = origHost;
        analyze();
    }
}
