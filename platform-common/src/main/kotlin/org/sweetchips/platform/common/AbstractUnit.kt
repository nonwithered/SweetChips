package org.sweetchips.platform.common

import java.nio.file.Path

abstract class AbstractUnit(
    override val input: Path,
    override val output: Path,
) : IUnit
