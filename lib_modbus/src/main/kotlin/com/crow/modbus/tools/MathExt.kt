package com.crow.modbus.tools

/**
 * ● 个
 *
 * ● 2023-10-10 09:47:12 周二 上午
 */
internal fun toUnit(value: Int) = value % 10

/**
 * ● 十
 *
 * ● 2023-10-10 09:48:04 周二 上午
 */
internal fun toTen(value: Int) = (value / 10) % 10

/**
 * ● 百
 *
 * ● 2023-10-10 09:48:28 周二 上午
 */
internal fun toHundred(value: Int) = (value / 100) % 10

/**
 * ● 千
 *
 * ● 2023-10-10 09:48:35 周二 上午
 */
internal fun toThousand(value: Int) = (value / 1000) % 10

/**
 * ● 万
 *
 * ● 2023-10-10 09:48:44 周二 上午
 */
internal fun toTenThousand(value: Int) = (value / 1_0000) % 10

/**
 * ● 十万
 *
 * ● 2023-10-10 09:48:50 周二 上午
 */
internal fun toHundredThousand(value: Int) = (value / 10_0000) % 10

/**
 * ● 百万
 *
 * ● 2023-10-10 09:48:57 周二 上午
 */
internal fun toMillion(value: Int) = (value / 100_0000) % 10

/**
 * ● 千万
 *
 * ● 2023-10-10 09:49:02 周二 上午
 */
internal fun toTenMillion(value: Int) = (value / 1000_0000) % 10
fun toTrillion(value: Int) = (value / 1_0000_0000) % 10

internal fun toTenTrillion(value: Int) = (value / 10_0000_0000) % 10